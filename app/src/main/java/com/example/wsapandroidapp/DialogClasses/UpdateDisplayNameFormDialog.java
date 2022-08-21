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
import com.example.wsapandroidapp.DataModel.User;
import com.example.wsapandroidapp.R;

import java.util.Collections;
import java.util.List;

public class UpdateDisplayNameFormDialog {

    EditText etDisplayName;
    TextView tvDisplayNameError;

    private final Context context;
    private Dialog dialog;

    private ComponentManager componentManager;

    String displayName;

    public UpdateDisplayNameFormDialog(Context context) {
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
        dialog.setContentView(R.layout.dialog_update_display_name_form_layout);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);

        ImageView imgClose = dialog.findViewById(R.id.imgClose);
        etDisplayName = dialog.findViewById(R.id.etDisplayName);
        tvDisplayNameError = dialog.findViewById(R.id.tvDisplayNameError);
        Button btnUpdate = dialog.findViewById(R.id.btnUpdate);

        List<TextView> errorTextViewList =
                Collections.singletonList(tvDisplayNameError);
        List<EditText> errorEditTextList =
                Collections.singletonList(etDisplayName);

        componentManager = new ComponentManager(context);
        componentManager.initializeErrorComponents(errorTextViewList, errorEditTextList);

        btnUpdate.setOnClickListener(view -> {
            componentManager.hideInputErrors();

            checkPersonName(displayName, true, context.getString(R.string.last_name), tvDisplayNameError, etDisplayName);

            if (componentManager.isNoInputError() && dialogListener != null)
                dialogListener.onUpdate(displayName);
        });

        imgClose.setOnClickListener(view -> dismissDialog());

        etDisplayName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                displayName = editable != null ? editable.toString() : "";

                componentManager.setInputRightDrawable(etDisplayName, !Credentials.isEmpty(displayName), Enums.CLEAR_TEXT);
                checkPersonName(displayName, true, context.getString(R.string.last_name), tvDisplayNameError, etDisplayName);
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

        etDisplayName.getText().clear();

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
        etDisplayName.setText(user.getLastName());
    }

    private DialogListener dialogListener;

    public interface DialogListener {
        void onUpdate(String displayName);
    }

    public void setDialogListener(DialogListener dialogListener) {
        this.dialogListener = dialogListener;
    }
}
