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
import com.example.wsapandroidapp.DataModel.User;
import com.example.wsapandroidapp.DataModel.UserRole;
import com.example.wsapandroidapp.DialogClasses.AppStatusPromptDialog;
import com.example.wsapandroidapp.DialogClasses.LoadingDialog;
import com.example.wsapandroidapp.DialogClasses.MessageDialog;
import com.example.wsapandroidapp.DialogClasses.NewVersionPromptDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class SignUpActivity extends AppCompatActivity {

    ImageView imgBack;
    EditText etLastName, etFirstName, etEmailAddress, etPassword, etConfirmPassword;
    Button btnSignUp;
    TextView tvSignIn, tvLastNameError, tvFirstNameError, tvEmailAddressError, tvPasswordError, tvConfirmPasswordError;

    Context context;

    LoadingDialog loadingDialog;
    MessageDialog messageDialog;

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;

    ComponentManager componentManager, componentManager2;

    String signUpLastName, signUpFirstName, signUpEmailAddress, signUpPassword, signUpConfirmPassword;

    boolean isPasswordShown = false, isConfirmPasswordShown = false;

    int tryCount;

    AppStatusPromptDialog appStatusPromptDialog;
    NewVersionPromptDialog newVersionPromptDialog;
    Query applicationQuery;
    boolean isListening;
    Application application = new Application();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        imgBack = findViewById(R.id.imgBack);
        etLastName = findViewById(R.id.etLastName);
        etFirstName = findViewById(R.id.etFirstName);
        etEmailAddress = findViewById(R.id.etEmailAddress);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSignUp = findViewById(R.id.btnSignUp);
        tvSignIn = findViewById(R.id.tvSignIn);
        tvLastNameError = findViewById(R.id.tvLastNameError);
        tvFirstNameError = findViewById(R.id.tvFirstNameError);
        tvEmailAddressError = findViewById(R.id.tvEmailAddressError);
        tvPasswordError = findViewById(R.id.tvPasswordError);
        tvConfirmPasswordError = findViewById(R.id.tvConfirmPasswordError);

        context = SignUpActivity.this;

        loadingDialog = new LoadingDialog(context);
        messageDialog = new MessageDialog(context);

        appStatusPromptDialog = new AppStatusPromptDialog(context);
        newVersionPromptDialog = new NewVersionPromptDialog(context);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance(getString(R.string.firebase_RTDB_url));
        applicationQuery = firebaseDatabase.getReference("application");

        isListening = true;
        applicationQuery.addValueEventListener(getApplicationValue());

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        List<TextView> errorTextViewList =
                Arrays.asList(tvLastNameError, tvFirstNameError, tvEmailAddressError, tvPasswordError, tvConfirmPasswordError);
        List<EditText> errorEditTextList =
                Arrays.asList(etLastName, etFirstName, etEmailAddress, etPassword, etConfirmPassword);

        componentManager = new ComponentManager(context);
        componentManager.initializeErrorComponents(errorTextViewList, errorEditTextList);
        componentManager2 = new ComponentManager(context);

        imgBack.setOnClickListener(view -> onBackPressed());

        etLastName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                signUpLastName = editable != null ? editable.toString() : "";

                componentManager.setInputRightDrawable(etLastName, !Credentials.isEmpty(signUpLastName), Enums.CLEAR_TEXT);
                checkPersonName(signUpLastName, true, getString(R.string.last_name), tvLastNameError, etLastName);
            }
        });

        etFirstName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                signUpFirstName = editable != null ? editable.toString() : "";

                componentManager.setInputRightDrawable(etFirstName, !Credentials.isEmpty(signUpFirstName), Enums.CLEAR_TEXT);
                checkPersonName(signUpFirstName, true, getString(R.string.first_name), tvFirstNameError, etFirstName);
            }
        });

        etEmailAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                signUpEmailAddress = editable != null ? editable.toString() : "";

                componentManager.setInputRightDrawable(etEmailAddress, !Credentials.isEmpty(signUpEmailAddress), Enums.CLEAR_TEXT);
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
                signUpPassword = editable != null ? editable.toString() : "";

                if (!isPasswordShown)
                    componentManager.setInputRightDrawable(etPassword, !Credentials.isEmpty(signUpPassword), Enums.SHOW_PASSWORD);
                else
                    componentManager.setInputRightDrawable(etPassword, !Credentials.isEmpty(signUpPassword), Enums.HIDE_PASSWORD);
                checkPasswordError();
            }
        });

        componentManager.setPasswordListener(isPasswordShownResult -> {
            isPasswordShown = isPasswordShownResult;
            if (!isPasswordShown)
                componentManager.setInputRightDrawable(etPassword, !Credentials.isEmpty(signUpPassword), Enums.SHOW_PASSWORD);
            else
                componentManager.setInputRightDrawable(etPassword, !Credentials.isEmpty(signUpPassword), Enums.HIDE_PASSWORD);
        });

        etConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                signUpConfirmPassword = editable != null ? editable.toString() : "";

                if (!isConfirmPasswordShown)
                    componentManager2.setInputRightDrawable(etConfirmPassword, !Credentials.isEmpty(signUpConfirmPassword), Enums.SHOW_PASSWORD);
                else
                    componentManager2.setInputRightDrawable(etConfirmPassword, !Credentials.isEmpty(signUpConfirmPassword), Enums.HIDE_PASSWORD);
                checkPasswordError();
            }
        });

        componentManager2.setPasswordListener(isPasswordShownResult -> {
            isConfirmPasswordShown = isPasswordShownResult;
            if (!isConfirmPasswordShown)
                componentManager2.setInputRightDrawable(etConfirmPassword, !Credentials.isEmpty(signUpConfirmPassword), Enums.SHOW_PASSWORD);
            else
                componentManager2.setInputRightDrawable(etConfirmPassword, !Credentials.isEmpty(signUpConfirmPassword), Enums.HIDE_PASSWORD);
        });

        etConfirmPassword.setOnEditorActionListener((textView, i, keyEvent) -> {
            boolean isHandled = false;
            if (i == EditorInfo.IME_ACTION_SEND) {
                btnSignUp.performClick();
                isHandled = true;
            }
            return isHandled;
        });

        btnSignUp.setOnClickListener(view -> signUp());

        tvSignIn.setOnClickListener(view -> {
            if (getIntent().getBooleanExtra("fromSignIn", false)) onBackPressed();
            else {
                Intent intent = new Intent(context, SignInActivity.class);
                intent.putExtra("fromSignUp", true);
                startActivity(intent);
            }
        });
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

    private void signUp() {
        componentManager.hideInputErrors();

        checkPersonName(signUpLastName, true, getString(R.string.last_name), tvLastNameError, etLastName);
        checkPersonName(signUpFirstName, true, getString(R.string.first_name), tvFirstNameError, etFirstName);
        checkEmailAddressError();
        checkPasswordError();

        if (componentManager.isNoInputError()) registerUser();
    }

    private void checkPersonName(String string, boolean isRequired, String fieldName,
                                 TextView targetTextView, EditText targetEditText) {
        componentManager.hideInputError(targetTextView, targetEditText);

        if (Credentials.isEmpty(string) && isRequired)
            componentManager.showInputError(targetTextView,
                    getString(R.string.required_input_error, fieldName),
                    targetEditText);
        else if (!Credentials.isValidLength(string, Credentials.REQUIRED_PERSON_NAME_LENGTH, 0))
            componentManager.showInputError(targetTextView,
                    Credentials.getSentenceCase(getString(R.string.length_error, "", Credentials.REQUIRED_PERSON_NAME_LENGTH)),
                    targetEditText);
    }

    private void checkEmailAddressError() {
        componentManager.hideInputError(tvEmailAddressError, etEmailAddress);

        if (Credentials.isEmpty(signUpEmailAddress))
            componentManager.showInputError(tvEmailAddressError,
                    getString(R.string.required_input_error, getString(R.string.email_address)),
                    etEmailAddress);
        else if (!Credentials.isValidEmailAddress(signUpEmailAddress))
            componentManager.showInputError(tvEmailAddressError,
                    getString(R.string.invalid_error, getString(R.string.email_address)),
                    etEmailAddress);
    }

    private void checkPasswordError() {
        componentManager.hideInputError(tvPasswordError, etPassword);
        componentManager.hideInputError(tvConfirmPasswordError, etConfirmPassword);

        if (Credentials.isEmpty(signUpPassword))
            componentManager.showInputError(tvPasswordError,
                    getString(R.string.required_input_error, getString(R.string.password)),
                    etPassword);
        else if (!Credentials.isValidLength(signUpPassword, Credentials.REQUIRED_PASSWORD_LENGTH, 0))
            componentManager.showInputError(tvPasswordError,
                    Credentials.getSentenceCase(getString(R.string.length_error, "", Credentials.REQUIRED_PASSWORD_LENGTH)),
                    etPassword);
        else if (!Credentials.isValidPassword(signUpPassword))
            componentManager.showInputError(tvPasswordError,
                    getString(R.string.invalid_error, getString(R.string.password)),
                    etPassword);
        else if (!Credentials.isPasswordMatch(signUpPassword, signUpConfirmPassword))
            componentManager.showInputError(tvPasswordError,
                    getString(R.string.match_error, getString(R.string.password)),
                    etPassword);

        if (Credentials.isEmpty(signUpConfirmPassword))
            componentManager.showInputError(tvConfirmPasswordError,
                    getString(R.string.required_input_error, getString(R.string.confirm_password)),
                    etConfirmPassword);
        else if (!Credentials.isPasswordMatch(signUpConfirmPassword, signUpPassword))
            componentManager.showInputError(tvConfirmPasswordError,
                    getString(R.string.match_error, getString(R.string.confirm_password)),
                    etConfirmPassword);
    }

    private void registerUser() {
        loadingDialog.setMessage(getString(R.string.sign_up_loading));
        loadingDialog.showDialog();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.createUserWithEmailAndPassword(signUpEmailAddress, signUpPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) addToDatabase();
                    else if (task.getException() != null)
                        signUpFailed(task.getException().toString());
                });
    }

    private void signUpFailed(String errorMsg) {
        loadingDialog.dismissDialog();

        String[] rawErrors = getResources().getStringArray(R.array.sign_up_raw_errors);
        String[] errors = getResources().getStringArray(R.array.sign_up_errors);

        if (Compare.containsIgnoreCase(errorMsg, rawErrors[0]))
            errorMsg = errors[0];

        messageDialog.setMessageType(Enums.ERROR_MESSAGE);
        messageDialog.setMessage(errorMsg);
        messageDialog.showDialog();
    }

    private void addToDatabase() {
        if (firebaseAuth != null) {
            firebaseUser = firebaseAuth.getCurrentUser();
            if (firebaseUser != null) {
                String photoUrl = "";
                if (firebaseUser.getPhotoUrl() != null)
                    photoUrl = firebaseUser.getPhotoUrl().toString();

                User user = new User(firebaseUser.getUid(), Enums.DEFAULT_AUTH_METHOD);
                user.setDisplayName(Credentials.fullTrim(signUpFirstName + " " + signUpLastName));
                user.setFirstName(Credentials.fullTrim(signUpFirstName));
                user.setLastName(Credentials.fullTrim(signUpLastName));
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
                                        tryCount = 0;
                                        sendEmailVerificationLink();
                                    } else rollbackUser();
                                });
                        else startMainActivity();
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

    private void startPostSignUpActivity(boolean isEmailVerificationLinkSent) {
        loadingDialog.dismissDialog();

        Intent intent = new Intent(context, PostSignUpActivity.class);
        intent.putExtra("isEmailVerificationLinkSent", isEmailVerificationLinkSent);
        intent.putExtra("fromSignUp", true);
        startActivity(intent);
        finishAffinity();
    }

    private void startMainActivity() {
        loadingDialog.dismissDialog();

        Intent intent = new Intent(context, MainActivity.class);
        startActivity(intent);
        finishAffinity();
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