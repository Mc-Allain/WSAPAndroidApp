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

import java.util.Collections;
import java.util.List;

public class ForgotPasswordFormDialog {

    private EditText etEmailAddress;
    private TextView tvEmailAddressError;

    private final Context context;
    private Dialog dialog;

    private ComponentManager componentManager;

    private String updateEmailAddress;

    public ForgotPasswordFormDialog(Context context) {
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
        dialog.setContentView(R.layout.dialog_forgot_password_form_layout);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);

        ImageView imgClose = dialog.findViewById(R.id.imgClose);
        etEmailAddress = dialog.findViewById(R.id.etEmailAddress);
        tvEmailAddressError = dialog.findViewById(R.id.tvEmailAddressError);
        Button btnUpdateEmail = dialog.findViewById(R.id.btnUpdateEmail);

        List<TextView> errorTextViewList =
                Collections.singletonList(tvEmailAddressError);
        List<EditText> errorEditTextList =
                Collections.singletonList(etEmailAddress);

        componentManager = new ComponentManager(context);
        componentManager.initializeErrorComponents(errorTextViewList, errorEditTextList);

        btnUpdateEmail.setOnClickListener(view -> {
            componentManager.hideInputErrors();

            checkEmailAddressError();

            if (componentManager.isNoInputError() && dialogListener != null)
                dialogListener.onUpdate(updateEmailAddress);
        });

        imgClose.setOnClickListener(view -> dismissDialog());

        etEmailAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                updateEmailAddress = editable != null ? editable.toString() : "";

                componentManager.setInputRightDrawable(etEmailAddress, !Credentials.isEmpty(updateEmailAddress), Enums.CLEAR_TEXT);
                checkEmailAddressError();
            }
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
    }

    private void checkEmailAddressError() {
        componentManager.hideInputError(tvEmailAddressError, etEmailAddress);

        if (Credentials.isEmpty(updateEmailAddress))
            componentManager.showInputError(tvEmailAddressError,
                    context.getString(R.string.required_input_error, context.getString(R.string.email_address)),
                    etEmailAddress);
        else if (!Credentials.isValidEmailAddress(updateEmailAddress))
            componentManager.showInputError(tvEmailAddressError,
                    context.getString(R.string.invalid_error, context.getString(R.string.email_address)),
                    etEmailAddress);
    }

    private DialogListener dialogListener;

    public interface DialogListener {
        void onUpdate(String emailAddress);
    }

    public void setDialogListener(DialogListener dialogListener) {
        this.dialogListener = dialogListener;
    }
}
