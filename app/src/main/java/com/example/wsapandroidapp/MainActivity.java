package com.example.wsapandroidapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.example.wsapandroidapp.Classes.Credentials;
import com.example.wsapandroidapp.Classes.Enums;
import com.example.wsapandroidapp.DataModel.Application;
import com.example.wsapandroidapp.DataModel.User;
import com.example.wsapandroidapp.DialogClasses.AppStatusPromptDialog;
import com.example.wsapandroidapp.DialogClasses.MessageDialog;
import com.example.wsapandroidapp.DialogClasses.NewVersionPromptDialog;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    NavHostFragment navHostFragment;
    NavController navController;

    Context context;

    MessageDialog messageDialog;

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    Query userQuery;

    boolean isListening;

    User user;

    String photoUrl;

    AppStatusPromptDialog appStatusPromptDialog;
    NewVersionPromptDialog newVersionPromptDialog;
    FirebaseDatabase firebaseDatabase;
    Query applicationQuery;
    Application application = new Application();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainerView);

        context = MainActivity.this;

        messageDialog = new MessageDialog(context);
        appStatusPromptDialog = new AppStatusPromptDialog(context);
        newVersionPromptDialog = new NewVersionPromptDialog(context);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null &&  firebaseUser.getPhotoUrl() != null)
            photoUrl = firebaseUser.getPhotoUrl().toString();

        firebaseDatabase = FirebaseDatabase.getInstance(getString(R.string.firebase_RTDB_url));
        userQuery = firebaseDatabase.getReference("users").orderByChild("id").equalTo(firebaseUser.getUid());
        applicationQuery = firebaseDatabase.getReference("application");

        isListening = true;
        userQuery.addValueEventListener(getUser());
        applicationQuery.addValueEventListener(getApplicationValue());

        /*for (int i = 0; i < 300; i++)
            Log.d("TAG", Credentials.getUniqueId());*/

        bottomNavigationView.setBackground(null);
        if (navHostFragment != null)
            navController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(bottomNavigationView, navController);
    }

    private void updatePhotoUrl() {
        userQuery.getRef().child(firebaseUser.getUid()).child("photoUrl").setValue(photoUrl);
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

                    if (user != null && Credentials.isEmpty(user.getPhotoUrl()))
                        updatePhotoUrl();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG: " + context.getClass(), "onCancelled", error.toException());

                messageDialog.setMessage(getString(R.string.fd_on_cancelled, "User"));
                messageDialog.setMessageType(Enums.ERROR_MESSAGE);
                messageDialog.showDialog();
            }
        };
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
}