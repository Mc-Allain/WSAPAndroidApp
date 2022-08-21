package com.example.wsapandroidapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.wsapandroidapp.Classes.Compare;
import com.example.wsapandroidapp.Classes.ComponentManager;
import com.example.wsapandroidapp.Classes.Enums;
import com.example.wsapandroidapp.Classes.Units;
import com.example.wsapandroidapp.DataModel.Application;
import com.example.wsapandroidapp.DialogClasses.AppStatusPromptDialog;
import com.example.wsapandroidapp.DialogClasses.LoadingDialog;
import com.example.wsapandroidapp.DialogClasses.MessageDialog;
import com.example.wsapandroidapp.DialogClasses.NewVersionPromptDialog;
import com.example.wsapandroidapp.DialogClasses.UpdateEmailAddressFormDialog;
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

public class PostSignUpActivity extends AppCompatActivity {

    TextView tvActivityTitle, tvPrompt, tvCountdown;
    Button btnResend, btnUpdateEmail, btnSignOut;
    ImageView imgClose, imgIcon;

    Context context;

    LoadingDialog loadingDialog;
    MessageDialog messageDialog;
    UpdateEmailAddressFormDialog updateEmailAddressFormDialog;

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    ComponentManager componentManager;

    String emailAddress;

    long targetDateTime;

    int tryCount;

    CountDownTimer validationTimer;

    AppStatusPromptDialog appStatusPromptDialog;
    NewVersionPromptDialog newVersionPromptDialog;
    FirebaseDatabase firebaseDatabase;
    Query applicationQuery;
    boolean isListening;
    Application application = new Application();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_sign_up);

        tvActivityTitle = findViewById(R.id.tvActivityTitle);
        tvPrompt = findViewById(R.id.tvPrompt);
        tvCountdown = findViewById(R.id.tvCountdown);
        btnResend = findViewById(R.id.btnResend);
        btnUpdateEmail = findViewById(R.id.btnUpdateEmail);
        btnSignOut = findViewById(R.id.btnSignOut);
        imgClose = findViewById(R.id.imgClose);
        imgIcon = findViewById(R.id.imgIcon);

        context = PostSignUpActivity.this;

        getSharedPreference();

        loadingDialog = new LoadingDialog(context);
        messageDialog = new MessageDialog(context);
        updateEmailAddressFormDialog = new UpdateEmailAddressFormDialog(context);

        updateEmailAddressFormDialog.setDialogListener(this::updateEmailAddress);

        appStatusPromptDialog = new AppStatusPromptDialog(context);
        newVersionPromptDialog = new NewVersionPromptDialog(context);

        componentManager = new ComponentManager(context);

        initCountDown();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance(getString(R.string.firebase_RTDB_url));
        applicationQuery = firebaseDatabase.getReference("application");

        isListening = true;
        applicationQuery.addValueEventListener(getApplicationValue());

        messageDialog.setMessage(getString(R.string.email_verification_link_failed));
        messageDialog.setMessageType(Enums.ERROR_MESSAGE);

        if (!getIntent().getBooleanExtra("isEmailVerificationLinkSent", true))
            messageDialog.showDialog();

        if (getIntent().getBooleanExtra("fromSignUp", false))
            tvActivityTitle.setText(getString(R.string.registration_successful));

        emailAddress = firebaseUser.getEmail();

        tvPrompt.setText(getResources().getString(R.string.email_verification_link_prompt,
                firebaseUser.getEmail()));

        btnResend.setOnClickListener(view -> {
            tryCount = 0;
            sendEmailVerificationLink();
        });

        btnUpdateEmail.setOnClickListener(view -> updateEmailAddressFormDialog.showDialog());

        btnSignOut.setOnClickListener(view -> signOut());

        imgClose.setOnClickListener(view -> signOut());
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
                        emailAddress = newEmailAddress;
                        sendEmailVerificationLink();

                        firebaseUser.reload();
                        tvPrompt.setText(getResources().getString(R.string.email_verification_link_prompt,
                                firebaseUser.getEmail()));

                        updateEmailAddressFormDialog.dismissDialog();
                    } else if (task.getException() != null)
                        updateEmailAddressFailed(task.getException().toString());
                });
    }

    private void updateEmailAddressFailed(String errorMsg) {
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

    private void sendEmailVerificationLink() {
        loadingDialog.showDialog();

        firebaseUser.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        loadingDialog.dismissDialog();

                        setSharedPreferences();
                        initCountDown();
                    } else {
                        if (tryCount == 5) {
                            loadingDialog.dismissDialog();
                            messageDialog.showDialog();
                        } else {
                            tryCount++;
                            sendEmailVerificationLink();
                        }
                    }
                });
    }

    private void signOut() {
        firebaseAuth.signOut();

        Intent intent = new Intent(context, WelcomeActivity.class);
        startActivity(intent);
        finishAffinity();
    }

    CountDownTimer countDownTimer;

    private void initCountDown() {
        long duration = targetDateTime - new Date().getTime();

        if (duration > 0) {
            tvCountdown.setVisibility(View.VISIBLE);
            componentManager.setPrimaryButtonEnabled(btnResend, false);

            countDownTimer = new CountDownTimer(duration, (long) Units.secToMs(1)) {
                @Override
                public void onTick(long l) {
                    long milliseconds = targetDateTime - new Date().getTime();

                    tvCountdown.setText(getString(R.string.email_verification_link_countdown,
                            (int) Units.msToSec(milliseconds)));
                }

                @Override
                public void onFinish() {
                    tvCountdown.setVisibility(View.GONE);
                    componentManager.setPrimaryButtonEnabled(btnResend, true);
                }
            };

            countDownTimer.start();
        }

        validationTimer = new CountDownTimer((long) Units.hourToMs(24), (long) Units.secToMs(1)) {
            @Override
            public void onTick(long l) {
                firebaseUser.reload();

                if (firebaseUser.isEmailVerified()) {
                    validationTimer.cancel();

                    loadingDialog.showDialog();

                    imgIcon.setImageResource(R.drawable.ic_baseline_check_circle_24);
                    imgIcon.setImageTintList(ColorStateList.valueOf(getColor(R.color.green)));

                    new Handler().postDelayed(() -> {
                        loadingDialog.dismissDialog();

                        tvCountdown.setVisibility(View.GONE);
                        componentManager.setPrimaryButtonEnabled(btnResend, true);
                        countDownTimer.cancel();

                        Intent intent = new Intent(context, MainActivity.class);
                        startActivity(intent);
                        finishAffinity();
                    }, (long) Units.secToMs(2));
                }
            }

            @Override
            public void onFinish() {
                validationTimer.start();
            }
        };
        validationTimer.start();
    }

    private void setSharedPreferences() {
        String group = getResources().getStringArray(R.array.shared_preferences)[0];
        String[] fields = getResources().getStringArray(R.array.auth_data);

        SharedPreferences sharedPreferences =
                context.getSharedPreferences(group, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        targetDateTime = (long) (new Date().getTime() + Units.minToMs(5) + Units.secToMs(1));

        editor.putLong(fields[1], targetDateTime);
        editor.apply();
    }

    private void getSharedPreference() {
        String group = getResources().getStringArray(R.array.shared_preferences)[0];
        String[] fields = getResources().getStringArray(R.array.auth_data);

        SharedPreferences sharedPreferences = getSharedPreferences(group, Context.MODE_PRIVATE);
        targetDateTime = sharedPreferences.getLong(fields[1], new Date().getTime());
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

        validationTimer.cancel();
    }
}