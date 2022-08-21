package com.example.wsapandroidapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wsapandroidapp.Classes.Enums;
import com.example.wsapandroidapp.DataModel.Application;
import com.example.wsapandroidapp.DataModel.User;
import com.example.wsapandroidapp.DataModel.UserRole;
import com.example.wsapandroidapp.DialogClasses.AppStatusPromptDialog;
import com.example.wsapandroidapp.DialogClasses.LoadingDialog;
import com.example.wsapandroidapp.DialogClasses.MessageDialog;
import com.example.wsapandroidapp.DialogClasses.NewVersionPromptDialog;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class WelcomeActivity extends AppCompatActivity {

    CardView googleSignInCard, defaultSignInCard;
    Button btnSkip;
    TextView tvSignUp;

    Context context;

    LoadingDialog loadingDialog;
    MessageDialog messageDialog;

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    GoogleSignInClient client;

    AppStatusPromptDialog appStatusPromptDialog;
    NewVersionPromptDialog newVersionPromptDialog;
    FirebaseDatabase firebaseDatabase;
    Query applicationQuery;
    boolean isListening;
    Application application = new Application();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        googleSignInCard = findViewById(R.id.googleSignInCard);
        defaultSignInCard = findViewById(R.id.defaultSignInCard);
        btnSkip = findViewById(R.id.btnSkip);
        tvSignUp = findViewById(R.id.tvSignUp);

        context = WelcomeActivity.this;

        loadingDialog = new LoadingDialog(context);
        messageDialog = new MessageDialog(context);

        appStatusPromptDialog = new AppStatusPromptDialog(context);
        newVersionPromptDialog = new NewVersionPromptDialog(context);

        if (getIntent().getBooleanExtra("isPasswordChanged", false)) {
            messageDialog.setMessage(getString(R.string.update_record_success_msg, "the password."));
            messageDialog.setMessageType(Enums.SUCCESS_MESSAGE);
            messageDialog.showDialog();
        }

        firebaseAuth = FirebaseAuth.getInstance();

        firebaseDatabase = FirebaseDatabase.getInstance(getString(R.string.firebase_RTDB_url));
        applicationQuery = firebaseDatabase.getReference("application");

        isListening = true;
        applicationQuery.addValueEventListener(getApplicationValue());

        GoogleSignInOptions options = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail().build();

        client = GoogleSignIn.getClient(this, options);

        googleSignInCard.setOnClickListener(view -> {
            Intent intent = client.getSignInIntent();
            startActivityForResult(intent, Enums.GOOGLE_REQUEST_CODE);
        });

        defaultSignInCard.setOnClickListener(view -> {
            Intent intent = new Intent(context, SignInActivity.class);
            startActivity(intent);
        });

        btnSkip.setOnClickListener(view -> signInAnonymously());

        tvSignUp.setOnClickListener(view -> {
            Intent intent = new Intent(context, SignUpActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Enums.GOOGLE_REQUEST_CODE && resultCode == RESULT_OK) {
            loadingDialog.showDialog();

            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                AuthCredential credential = GoogleAuthProvider
                        .getCredential(account.getIdToken(), null);

                firebaseAuth.signInWithCredential(credential)
                        .addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) addToDatabase();
                            else {
                                Toast.makeText(
                                        context,
                                        "Failed to sign in with google",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        });
            } catch (ApiException e) {
                e.printStackTrace();
            }
        }
    }

    private void addToDatabase() {
        if (firebaseAuth != null) {
            firebaseUser = firebaseAuth.getCurrentUser();
            if (firebaseUser != null) {
                String photoUrl = "";
                if (firebaseUser.getPhotoUrl() != null)
                    photoUrl = firebaseUser.getPhotoUrl().toString();

                User user = new User(firebaseUser.getUid(), Enums.GOOGLE_AUTH_METHOD);
                user.setDisplayName(firebaseUser.getDisplayName());
                user.setPhotoUrl(photoUrl);
                user.setRole(new UserRole(false, false, false, false));

                DatabaseReference userRef = firebaseDatabase.getReference("users").child(firebaseUser.getUid());

                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists())
                            userRef.setValue(user)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        setSharedPreferences(false);
                                        startMainActivity();
                                    } else rollbackUser();
                                });
                        else {
                            setSharedPreferences(false);
                            startMainActivity();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("TAG: " + context.getClass(), "onCancelled", error.toException());
                        loadingDialog.dismissDialog();

                        messageDialog.setMessage(getString(R.string.fd_on_cancelled, "User"));
                        messageDialog.setMessageType(Enums.ERROR_MESSAGE);
                        messageDialog.showDialog();
                    }
                });
            }
        }
    }

    private void rollbackUser() {
        if (firebaseUser != null) {
            firebaseUser.delete().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    loadingDialog.dismissDialog();

                    messageDialog.setMessage(getString(R.string.sign_up_error));
                    messageDialog.setMessageType(Enums.ERROR_MESSAGE);
                    messageDialog.showDialog();
                } else addToDatabase();
            });
        }
    }

    private void signInAnonymously() {
        loadingDialog.showDialog();

        firebaseAuth.signInAnonymously().addOnSuccessListener(authResult -> {
            setSharedPreferences(true);
            startMainActivity();
        }).addOnFailureListener(e -> {
            loadingDialog.dismissDialog();

            messageDialog.setMessageType(Enums.ERROR_MESSAGE);
            messageDialog.setMessage(getString(R.string.anonymous_sign_in_error));
            messageDialog.showDialog();
        });
    }

    private void startMainActivity() {
        loadingDialog.dismissDialog();

        Intent intent = new Intent(context, MainActivity.class);
        startActivity(intent);
        finishAffinity();
    }

    private void setSharedPreferences(boolean isAnonymous) {
        String group = getResources().getStringArray(R.array.shared_preferences)[0];
        String[] fields = getResources().getStringArray(R.array.auth_data);

        SharedPreferences sharedPreferences =
                context.getSharedPreferences(group, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean(fields[2], isAnonymous);
        editor.apply();
    }

    private ValueEventListener getApplicationValue() {
        return new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
                    appStatusPromptDialog.dismissDialog();
                    newVersionPromptDialog.dismissDialog();

                    if (snapshot.exists()) {
                        application = snapshot.getValue(Application.class);

                        if (application != null) {
                            if (!application.getStatus().equals(getString(R.string.live)) && !application.isForDeveloper()) {
                                appStatusPromptDialog.setTitle(application.getStatus());
                                appStatusPromptDialog.showDialog();
                            } else if (application.getCurrentVersion() < application.getLatestVersion()
                                    && !application.isForDeveloper()) {
                                newVersionPromptDialog.setVersion(application.getCurrentVersion(), application.getLatestVersion());
                                newVersionPromptDialog.showDialog();
                            }

                            appStatusPromptDialog.setDialogListener(() -> {
                                appStatusPromptDialog.dismissDialog();
                                finish();
                            });

                            newVersionPromptDialog.setDialogListener(new NewVersionPromptDialog.DialogListener() {
                                @Override
                                public void onUpdate() {
                                    Intent intent = new Intent("android.intent.action.VIEW",
                                            Uri.parse(application.getDownloadLink()));
                                    startActivity(intent);

                                    newVersionPromptDialog.dismissDialog();
                                    finish();
                                }

                                @Override
                                public void onCancel() {
                                    newVersionPromptDialog.dismissDialog();
                                    finish();
                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG: " + context.getClass(), "onCancelled", error.toException());
            }
        };
    }

    @Override
    public void onResume() {
        isListening = true;
        applicationQuery.addListenerForSingleValueEvent(getApplicationValue());

        super.onResume();
    }

    @Override
    public void onStop() {
        isListening = false;

        super.onStop();
    }

    @Override
    public void onDestroy() {
        isListening = false;

        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            startMainActivity();
        }
    }
}