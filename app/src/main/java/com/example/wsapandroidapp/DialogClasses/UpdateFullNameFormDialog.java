package com.example.wsapandroidapp.DialogClasses;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.wsapandroidapp.Classes.ComponentManager;
import com.example.wsapandroidapp.Classes.Credentials;
import com.example.wsapandroidapp.Classes.Enums;
import com.example.wsapandroidapp.DataModel.Chapter;
import com.example.wsapandroidapp.DataModel.Supplier;
import com.example.wsapandroidapp.DataModel.User;
import com.example.wsapandroidapp.R;

import java.util.Arrays;
import java.util.List;

public class UpdateFullNameFormDialog {

    EditText etLastName, etFirstName;
    TextView tvLastNameError, tvFirstNameError;

    private final Context context;
    private Dialog dialog;

    private ComponentManager componentManager;

    String signUpLastName, signUpFirstName;

    public UpdateFullNameFormDialog(Context context) {
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
        dialog.setContentView(R.layout.dialog_update_full_name_form_layout);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);

        ImageView imgClose = dialog.findViewById(R.id.imgClose);
        etLastName = dialog.findViewById(R.id.etLastName);
        etFirstName = dialog.findViewById(R.id.etFirstName);
        tvLastNameError = dialog.findViewById(R.id.tvLastNameError);
        tvFirstNameError = dialog.findViewById(R.id.tvFirstNameError);
        Button btnUpdate = dialog.findViewById(R.id.btnUpdate);

        List<TextView> errorTextViewList =
                Arrays.asList(tvLastNameError, tvFirstNameError);
        List<EditText> errorEditTextList =
                Arrays.asList(etLastName, etFirstName);

        componentManager = new ComponentManager(context);
        componentManager.initializeErrorComponents(errorTextViewList, errorEditTextList);

        btnUpdate.setOnClickListener(view -> {
            componentManager.hideInputErrors();

            checkPersonName(signUpLastName, true, context.getString(R.string.last_name), tvLastNameError, etLastName);
            checkPersonName(signUpFirstName, true, context.getString(R.string.first_name), tvFirstNameError, etFirstName);

            if (componentManager.isNoInputError() && dialogListener != null)
                dialogListener.onUpdate(signUpLastName, signUpFirstName);
        });

        imgClose.setOnClickListener(view -> dismissDialog());

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
                checkPersonName(signUpLastName, true, context.getString(R.string.last_name), tvLastNameError, etLastName);
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
                checkPersonName(signUpFirstName, true, context.getString(R.string.first_name), tvFirstNameError, etFirstName);
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

        etLastName.getText().clear();
        etFirstName.getText().clear();

        componentManager.hideInputErrors();
    }

    private void checkPersonName(String string, boolean isRequired, String fieldName,
                                 TextView targetTextView, EditText targetEditText) {
        componentManager.hideInputError(targetTextView, targetEditText);

        if (Credentials.isEmpty(string) && isRequired)
            componentManager.showInputError(targetTextView,
                    context.getString(R.string.required_input_error, fieldName),
                    targetEditText);
        else if (!Credentials.isValidLength(string, Credentials.REQUIRED_PERSON_NAME_LENGTH, 0))
            componentManager.showInputError(targetTextView,
                    Credentials.getSentenceCase(context.getString(R.string.length_error, "", Credentials.REQUIRED_PERSON_NAME_LENGTH)),
                    targetEditText);
    }

    public void setUser(User user) {
        etLastName.setText(user.getLastName());
        etFirstName.setText(user.getFirstName());
    }

    private DialogListener dialogListener;

    public interface DialogListener {
        void onUpdate(String lastName, String firstName);
    }

    public void setDialogListener(DialogListener dialogListener) {
        this.dialogListener = dialogListener;
    }
}
