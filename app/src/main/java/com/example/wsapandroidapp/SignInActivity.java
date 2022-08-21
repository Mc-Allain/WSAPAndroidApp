package com.example.wsapandroidapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.wsapandroidapp.Classes.Compare;
import com.example.wsapandroidapp.Classes.ComponentManager;
import com.example.wsapandroidapp.Classes.Credentials;
import com.example.wsapandroidapp.Classes.Enums;
import com.example.wsapandroidapp.Classes.Units;
import com.example.wsapandroidapp.DataModel.Application;
import com.example.wsapandroidapp.DialogClasses.AppStatusPromptDialog;
import com.example.wsapandroidapp.DialogClasses.ForgotPasswordFormDialog;
import com.example.wsapandroidapp.DialogClasses.LoadingDialog;
import com.example.wsapandroidapp.DialogClasses.MessageDialog;
import com.example.wsapandroidapp.DialogClasses.NewVersionPromptDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class SignInActivity extends AppCompatActivity {

    ImageView imgBack;
    EditText etEmailAddress, etPassword;
    Button btnSignIn;
    TextView tvSignUp, tvEmailAddressError, tvPasswordError, tvForgotPassword;

    Context context;

    LoadingDialog loadingDialog;
    MessageDialog messageDialog;
    ForgotPasswordFormDialog forgotPasswordFormDialog;

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    ComponentManager componentManager;

    String signInEmailAddress, signInPassword;

    boolean isPasswordShown = false;

    AppStatusPromptDialog appStatusPromptDialog;
    NewVersionPromptDialog newVersionPromptDialog;
    FirebaseDatabase firebaseDatabase;
    Query applicationQuery;
    boolean isListening;
    Application application = new Application();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        imgBack = findViewById(R.id.imgBack);
        etEmailAddress = findViewById(R.id.etEmailAddress);
        etPassword = findViewById(R.id.etPassword);
        btnSignIn = findViewById(R.id.btnSignIn);
        tvSignUp = findViewById(R.id.tvSignUp);
        tvEmailAddressError = findViewById(R.id.tvEmailAddressError);
        tvPasswordError = findViewById(R.id.tvPasswordError);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        context = SignInActivity.this;

        loadingDialog = new LoadingDialog(context);
        messageDialog = new MessageDialog(context);
        forgotPasswordFormDialog = new ForgotPasswordFormDialog(context);

        forgotPasswordFormDialog.setDialogListener(this::checkEmailAddressRegistration);

        appStatusPromptDialog = new AppStatusPromptDialog(context);
        newVersionPromptDialog = new NewVersionPromptDialog(context);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance(getString(R.string.firebase_RTDB_url));
        applicationQuery = firebaseDatabase.getReference("application");

        isListening = true;
        applicationQuery.addValueEventListener(getApplicationValue());

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        List<TextView> errorTextViewList = Arrays.asList(tvEmailAddressError, tvPasswordError);
        List<EditText> errorEditTextList = Arrays.asList(etEmailAddress, etPassword);

        componentManager = new ComponentManager(context);
        componentManager.initializeErrorComponents(errorTextViewList, errorEditTextList);

        imgBack.setOnClickListener(view -> onBackPressed());

        etEmailAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                signInEmailAddress = editable != null ? editable.toString() : "";

                componentManager.setInputRightDrawable(etEmailAddress, !Credentials.isEmpty(signInEmailAddress), Enums.CLEAR_TEXT);
                checkEmailAddressError();
            }
        });

        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                signInPassword = editable != null ? editable.toString() : "";

                if (!isPasswordShown)
                    componentManager.setInputRightDrawable(etPassword, !Credentials.isEmpty(signInPassword), Enums.SHOW_PASSWORD);
                else
                    componentManager.setInputRightDrawable(etPassword, !Credentials.isEmpty(signInPassword), Enums.HIDE_PASSWORD);
                checkPasswordError();
            }
        });

        etPassword.setOnEditorActionListener((textView, i, keyEvent) -> {
            boolean isHandled = false;
            if (i == EditorInfo.IME_ACTION_SEND) {
                btnSignIn.performClick();
                isHandled = true;
            }
            return isHandled;
        });

        componentManager.setPasswordListener(isPasswordShownResult -> {
            isPasswordShown = isPasswordShownResult;
            if (!isPasswordShown)
                componentManager.setInputRightDrawable(etPassword, !Credentials.isEmpty(signInPassword), Enums.SHOW_PASSWORD);
            else
                componentManager.setInputRightDrawable(etPassword, !Credentials.isEmpty(signInPassword), Enums.HIDE_PASSWORD);
        });

        btnSignIn.setOnClickListener(view -> defaultSignIn());

        tvSignUp.setOnClickListener(view -> {
            if (getIntent().getBooleanExtra("fromSignUp", false)) onBackPressed();
            else {
                Intent intent = new Intent(context, SignUpActivity.class);
                intent.putExtra("fromSignIn", true);
                startActivity(intent);
            }
        });

        tvForgotPassword.setOnClickListener(view -> forgotPasswordFormDialog.showDialog());
    }

    private void checkEmailAddressRegistration(String emailAddress) {
        loadingDialog.showDialog();

        firebaseAuth.fetchSignInMethodsForEmail(emailAddress)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().getSignInMethods() != null &&
                                task.getResult().getSignInMethods().isEmpty()) {
                            loadingDialog.dismissDialog();

                            messageDialog.setMessageType(Enums.ERROR_MESSAGE);
                            messageDialog.setMessage(getString(R.string.forgot_password_failed));
                            messageDialog.showDialog();
                        } else sendForgotPasswordEmailLink(emailAddress);
                    } else if (task.getException() != null)
                        signInFailed(task.getException().toString());
                });
    }
    private void sendForgotPasswordEmailLink(String emailAddress) {
        firebaseAuth.sendPasswordResetEmail(emailAddress)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        loadingDialog.dismissDialog();
                        forgotPasswordFormDialog.dismissDialog();

                        messageDialog.setMessageType(Enums.SUCCESS_MESSAGE);
                        messageDialog.setMessage(getString(R.string.forgot_password_success));
                        messageDialog.showDialog();
                    } else if (task.getException() != null)
                        signInFailed(task.getException().toString());
                });
    }

    private void defaultSignIn() {
        componentManager.hideInputErrors();

        checkEmailAddressError();
        checkPasswordError();

        if (componentManager.isNoInputError()) {
            loadingDialog.showDialog();
            firebaseAuth.signInWithEmailAndPassword(signInEmailAddress, signInPassword)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) startMainActivity();
                        else if (task.getException() != null)
                            signInFailed(task.getException().toString());
                    });
        }
    }

    private void signInFailed(String errorMsg) {
        loadingDialog.dismissDialog();

        String[] rawErrors = getResources().getStringArray(R.array.sign_in_raw_errors);
        String[] errors = getResources().getStringArray(R.array.sign_in_errors);

        if (Compare.containsIgnoreCase(errorMsg, rawErrors[0]) ||
                Compare.containsIgnoreCase(errorMsg, rawErrors[1]))
            errorMsg = errors[0];
        else if (Compare.containsIgnoreCase(errorMsg, rawErrors[2]))
            errorMsg = errors[1];
        else if (Compare.containsIgnoreCase(errorMsg, rawErrors[3]))
            errorMsg = errors[2];
        else if (Compare.containsIgnoreCase(errorMsg, rawErrors[4]))
            errorMsg = errors[3];
        else if (Compare.containsIgnoreCase(errorMsg, rawErrors[5]))
            errorMsg = errors[4];

        messageDialog.setMessageType(Enums.ERROR_MESSAGE);
        messageDialog.setMessage(errorMsg);
        messageDialog.showDialog();
    }

    private void checkEmailAddressError() {
        componentManager.hideInputError(tvEmailAddressError, etEmailAddress);

        if (Credentials.isEmpty(signInEmailAddress))
            componentManager.showInputError(tvEmailAddressError,
                    getString(R.string.required_input_error, getString(R.string.email_address)),
                    etEmailAddress);
        else if (!Credentials.isValidEmailAddress(signInEmailAddress))
            componentManager.showInputError(tvEmailAddressError,
                    getString(R.string.invalid_error, getString(R.string.email_address)),
                    etEmailAddress);
    }

    private void checkPasswordError() {
        componentManager.hideInputError(tvPasswordError, etPassword);

        if (Credentials.isEmpty(signInPassword))
            componentManager.showInputError(tvPasswordError,
                    getString(R.string.required_input_error, getString(R.string.password)),
                    etPassword);
    }

    private void startMainActivity() {
        loadingDialog.dismissDialog();

        setSharedPreferences();

        firebaseUser = firebaseAuth.getCurrentUser();

        Intent intent = new Intent(context, MainActivity.class);

        if (firebaseUser != null && !firebaseUser.isEmailVerified())
            intent = new Intent(context, PostSignUpActivity.class);

        startActivity(intent);
        finishAffinity();
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

    private void setSharedPreferences() {
        String group = getResources().getStringArray(R.array.shared_preferences)[0];
        String[] fields = getResources().getStringArray(R.array.auth_data);

        SharedPreferences sharedPreferences =
                context.getSharedPreferences(group, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(fields[0], signInPassword);
        editor.putBoolean(fields[2], false);
        editor.apply();
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