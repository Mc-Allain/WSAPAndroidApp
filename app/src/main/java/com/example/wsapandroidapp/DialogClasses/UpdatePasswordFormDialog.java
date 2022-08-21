package com.example.wsapandroidapp.DialogClasses;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.wsapandroidapp.Classes.ComponentManager;
import com.example.wsapandroidapp.Classes.Credentials;
import com.example.wsapandroidapp.Classes.Enums;
import com.example.wsapandroidapp.R;

import java.util.Arrays;
import java.util.List;

public class UpdatePasswordFormDialog {

    EditText etPassword, etConfirmPassword;
    TextView tvPasswordError, tvConfirmPasswordError;

    private final Context context;
    private Dialog dialog;

    ComponentManager componentManager, componentManager2;

    String signUpPassword, signUpConfirmPassword;

    boolean isPasswordShown = false, isConfirmPasswordShown = false;

    public UpdatePasswordFormDialog(Context context) {
        this.context = context;

        createDialog();
    }

    private void createDialog() {
        setDialog();
        setDialogWindow();
    }

    private void setDialog() {
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_update_password_form_layout);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);

        ImageView imgClose = dialog.findViewById(R.id.imgClose);
        etPassword = dialog.findViewById(R.id.etPassword);
        etConfirmPassword = dialog.findViewById(R.id.etConfirmPassword);
        tvPasswordError = dialog.findViewById(R.id.tvPasswordError);
        tvConfirmPasswordError = dialog.findViewById(R.id.tvConfirmPasswordError);
        Button btnUpdate = dialog.findViewById(R.id.btnUpdate);

        List<TextView> errorTextViewList =
                Arrays.asList(tvPasswordError, tvConfirmPasswordError);
        List<EditText> errorEditTextList =
                Arrays.asList(etPassword, etConfirmPassword);

        componentManager = new ComponentManager(context);
        componentManager.initializeErrorComponents(errorTextViewList, errorEditTextList);
        componentManager2 = new ComponentManager(context);

        btnUpdate.setOnClickListener(view -> {
            componentManager.hideInputErrors();

            checkPasswordError();

            if (componentManager.isNoInputError() && dialogListener != null)
                dialogListener.onUpdate(signUpPassword);
        });

        imgClose.setOnClickListener(view -> dismissDialog());

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
    }

    private void setDialogWindow() {
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    public void showDialog() {
        dialog.show();
    }

    public void dismissDialog() {
        dialog.dismiss();

        etPassword.getText().clear();
        etConfirmPassword.getText().clear();

        isPasswordShown = false;
        isConfirmPasswordShown = false;

        componentManager.hideInputErrors();
    }

    private void checkPasswordError() {
        componentManager.hideInputError(tvPasswordError, etPassword);
        componentManager.hideInputError(tvConfirmPasswordError, etConfirmPassword);

        if (Credentials.isEmpty(signUpPassword))
            componentManager.showInputError(tvPasswordError,
                    context.getString(R.string.required_input_error, context.getString(R.string.password)),
                    etPassword);
        else if (!Credentials.isValidLength(signUpPassword, Credentials.REQUIRED_PASSWORD_LENGTH, 0))
            componentManager.showInputError(tvPasswordError,
                    Credentials.getSentenceCase(context.getString(R.string.length_error, "", Credentials.REQUIRED_PASSWORD_LENGTH)),
                    etPassword);
        else if (!Credentials.isValidPassword(signUpPassword))
            componentManager.showInputError(tvPasswordError,
                    context.getString(R.string.invalid_error, context.getString(R.string.password)),
                    etPassword);
        else if (!Credentials.isPasswordMatch(signUpPassword, signUpConfirmPassword))
            componentManager.showInputError(tvPasswordError,
                    context.getString(R.string.match_error, context.getString(R.string.password)),
                    etPassword);

        if (Credentials.isEmpty(signUpConfirmPassword))
            componentManager.showInputError(tvConfirmPasswordError,
                    context.getString(R.string.required_input_error, context.getString(R.string.confirm_password)),
                    etConfirmPassword);
        else if (!Credentials.isPasswordMatch(signUpConfirmPassword, signUpPassword))
            componentManager.showInputError(tvConfirmPasswordError,
                    context.getString(R.string.match_error, context.getString(R.string.confirm_password)),
                    etConfirmPassword);
    }

    private DialogListener dialogListener;

    public interface DialogListener {
        void onUpdate(String signUpPassword);
    }

    public void setDialogListener(DialogListener dialogListener) {
        this.dialogListener = dialogListener;
    }
}
