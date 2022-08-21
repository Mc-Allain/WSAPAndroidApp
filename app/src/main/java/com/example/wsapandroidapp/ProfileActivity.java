package com.example.wsapandroidapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.wsapandroidapp.Classes.Compare;
import com.example.wsapandroidapp.Classes.Credentials;
import com.example.wsapandroidapp.Classes.Enums;
import com.example.wsapandroidapp.Classes.Units;
import com.example.wsapandroidapp.DataModel.Application;
import com.example.wsapandroidapp.DataModel.User;
import com.example.wsapandroidapp.DialogClasses.AppStatusPromptDialog;
import com.example.wsapandroidapp.DialogClasses.LoadingDialog;
import com.example.wsapandroidapp.DialogClasses.MessageDialog;
import com.example.wsapandroidapp.DialogClasses.NewVersionPromptDialog;
import com.example.wsapandroidapp.DialogClasses.UpdateDisplayNameFormDialog;
import com.example.wsapandroidapp.DialogClasses.UpdateEmailAddressFormDialog;
import com.example.wsapandroidapp.DialogClasses.UpdateFullNameFormDialog;
import com.example.wsapandroidapp.DialogClasses.UpdatePasswordFormDialog;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    ImageView imgDisplay, imgUpdateDisplayName, imgUpdateEmailAddress, imgUpdatePassword;
    TextView tvDisplayName, tvEmailAddress2;

    Context context;

    LoadingDialog loadingDialog;
    MessageDialog messageDialog;
    UpdateFullNameFormDialog updateFullNameFormDialog;
    UpdateDisplayNameFormDialog updateDisplayNameFormDialog;
    UpdateEmailAddressFormDialog updateEmailAddressFormDialog;
    UpdatePasswordFormDialog updatePasswordFormDialog;

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;

    Query userQuery;

    boolean isListening;

    User user;

    String photoUrl;

    String emailAddress, signUpPassword;

    int tryCount;

    AppStatusPromptDialog appStatusPromptDialog;
    NewVersionPromptDialog newVersionPromptDialog;
    Query applicationQuery;
    Application application = new Application();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        imgDisplay = findViewById(R.id.imgDisplay);
        imgUpdateDisplayName = findViewById(R.id.imgUpdateDisplayName);
        tvDisplayName = findViewById(R.id.tvDisplayName);
        tvEmailAddress2 = findViewById(R.id.tvEmailAddress2);
        imgUpdateEmailAddress = findViewById(R.id.imgUpdateEmailAddress);
        imgUpdatePassword = findViewById(R.id.imgUpdatePassword);

        context = ProfileActivity.this;

        loadingDialog = new LoadingDialog(context);
        messageDialog = new MessageDialog(context);
        updateFullNameFormDialog = new UpdateFullNameFormDialog(context);

        updateFullNameFormDialog.setDialogListener((lastName, firstName) -> {
            User newUser = user;
            newUser.setLastName(Credentials.fullTrim(lastName));
            newUser.setFirstName(Credentials.fullTrim(firstName));
            newUser.setDisplayName(Credentials.fullTrim(firstName + " " + lastName));
            updateUser(newUser);
        });

        updateDisplayNameFormDialog = new UpdateDisplayNameFormDialog(context);

        updateDisplayNameFormDialog.setDialogListener(displayName -> {
            User newUser = user;
            newUser.setLastName(Credentials.fullTrim(displayName));
            updateUser(newUser);
        });

        updateEmailAddressFormDialog = new UpdateEmailAddressFormDialog(context);

        updateEmailAddressFormDialog.setDialogListener(emailAddress -> {
            tryCount = 0;
            updateEmailAddress(emailAddress);
        });

        updatePasswordFormDialog = new UpdatePasswordFormDialog(context);

        updatePasswordFormDialog.setDialogListener(password -> {
            signUpPassword = password;
            updatePassword(password);
        });

        appStatusPromptDialog = new AppStatusPromptDialog(context);
        newVersionPromptDialog = new NewVersionPromptDialog(context);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null &&  firebaseUser.getPhotoUrl() != null)
            photoUrl = firebaseUser.getPhotoUrl().toString();

        firebaseDatabase = FirebaseDatabase.getInstance(getString(R.string.firebase_RTDB_url));
        userQuery = firebaseDatabase.getReference("users").orderByChild("id").equalTo(firebaseUser.getUid());
        applicationQuery = firebaseDatabase.getReference("application");

        loadingDialog.showDialog();
        isListening = true;
        userQuery.addValueEventListener(getUser());
        applicationQuery.addValueEventListener(getApplicationValue());

        imgUpdateDisplayName.setOnClickListener(view -> {
            if (user.getAuthMethod().equals(Enums.DEFAULT_AUTH_METHOD)) {
                updateFullNameFormDialog.setUser(user);
                updateFullNameFormDialog.showDialog();
            } else {
                updateDisplayNameFormDialog.setUser(user);
                updateDisplayNameFormDialog.showDialog();
            }
        });

        imgUpdateEmailAddress.setOnClickListener(view -> updateEmailAddressFormDialog.showDialog());

        imgUpdatePassword.setOnClickListener(view -> updatePasswordFormDialog.showDialog());

        imgUpdateEmailAddress.setVisibility(View.GONE);
        imgUpdatePassword.setVisibility(View.GONE);
    }

    private ValueEventListener getUser() {
        return new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {

                    if (snapshot.exists())
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            user = dataSnapshot.getValue(User.class);
                            break;
                        }

                    loadingDialog.dismissDialog();

                    updateUI();
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
        };
    }

    private void updateUI() {
        if (photoUrl != null)
            Glide.with(context).load(photoUrl).centerCrop().placeholder(R.drawable.ic_wsap).
                    error(R.drawable.ic_wsap).into(imgDisplay);
        else imgDisplay.setImageResource(R.drawable.ic_wsap);

        tvDisplayName.setText(user.getDisplayName());
        emailAddress = firebaseUser.getEmail();
        tvEmailAddress2.setText(emailAddress);

        if (user.getAuthMethod().equals(Enums.DEFAULT_AUTH_METHOD)) {
            imgUpdateEmailAddress.setVisibility(View.VISIBLE);
            imgUpdatePassword.setVisibility(View.VISIBLE);
        }
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

    private void updateUser(User newUser) {
        loadingDialog.showDialog();

        userQuery.getRef().child(firebaseUser.getUid())
                .setValue(newUser).addOnCompleteListener(task -> {
                    loadingDialog.dismissDialog();
                    updateFullNameFormDialog.dismissDialog();
                    updateDisplayNameFormDialog.dismissDialog();

                    if (task.isSuccessful()) {
                        messageDialog.setMessageType(Enums.SUCCESS_MESSAGE);
                        messageDialog.setMessage(getString(R.string.update_record_success_msg, "the display name."));
                    } else if (task.getException() != null) {
                        messageDialog.setMessageType(Enums.ERROR_MESSAGE);
                        messageDialog.setMessage(task.getException().toString());
                    }

                    messageDialog.showDialog();
                });
    }

    private void updateEmailAddress(String newEmailAddress) {
        if (newEmailAddress.equals(emailAddress)) {
            messageDialog.setMessageType(Enums.ERROR_MESSAGE);
            messageDialog.setMessage(getString(R.string.same_email_address_error));
            messageDialog.showDialog();
            return;
        }

        loadingDialog.showDialog();

        firebaseUser.updateEmail(newEmailAddress)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        updateEmailAddressFormDialog.dismissDialog();
                        sendEmailVerificationLink();
                    } else if (task.getException() != null)
                        updateError(task.getException().toString());
                });
    }

    private void sendEmailVerificationLink() {
        firebaseUser.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        setSharedPreferences();
                        startPostSignUpActivity(true);
                    } else {
                        if (tryCount == 5)
                            startPostSignUpActivity(false);
                        else {
                            tryCount++;
                            sendEmailVerificationLink();
                        }
                    }
                });
    }

    private void startPostSignUpActivity(boolean isEmailVerificationLinkSent) {
        loadingDialog.dismissDialog();

        Intent intent = new Intent(context, PostSignUpActivity.class);
        intent.putExtra("isEmailVerificationLinkSent", isEmailVerificationLinkSent);
        intent.putExtra("fromSignUp", false);
        startActivity(intent);
        finishAffinity();
    }

    private void updatePassword(String password) {
        loadingDialog.showDialog();

        firebaseUser.updatePassword(password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        updatePasswordFormDialog.dismissDialog();
                        signOut();
                    } else if (task.getException() != null)
                        updateError(task.getException().toString());
                });
    }

    private void signOut() {
        GoogleSignInOptions options = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail().build();

        GoogleSignInClient client = GoogleSignIn.getClient(context, options);

        client.signOut();

        firebaseAuth.signOut();

        Intent intent = new Intent(context, WelcomeActivity.class);
        intent.putExtra("isPasswordChanged", true);
        startActivity(intent);
        finishAffinity();
    }

    private void updateError(String errorMsg) {
        loadingDialog.dismissDialog();

        String[] rawErrors = getResources().getStringArray(R.array.sign_up_raw_errors);
        String[] errors = getResources().getStringArray(R.array.sign_up_errors);

        if (Compare.containsIgnoreCase(errorMsg, rawErrors[0]))
            errorMsg = errors[0];
        else if (Compare.containsIgnoreCase(errorMsg, rawErrors[1]))
            errorMsg = errors[1];

        messageDialog.setMessageType(Enums.ERROR_MESSAGE);
        messageDialog.setMessage(errorMsg);
        messageDialog.showDialog();
    }

    private void setSharedPreferences() {
        String group = getResources().getStringArray(R.array.shared_preferences)[0];
        String[] fields = getResources().getStringArray(R.array.auth_data);

        SharedPreferences sharedPreferences =
                context.getSharedPreferences(group, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        long targetDateTime = (long) (new Date().getTime() + Units.minToMs(5) + Units.secToMs(1));

        editor.putString(fields[0], signUpPassword);
        editor.putLong(fields[1], targetDateTime);
        editor.putBoolean(fields[2], false);
        editor.apply();
    }

    @Override
    public void onResume() {
        isListening = true;
        userQuery.addListenerForSingleValueEvent(getUser());
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
}