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
import com.google.firebase.database.DatabaseReference;

public class DescriptionFormDialog {

    private EditText etDescription;
    private TextView tvMessageTitle;

    private final Context context;
    private Dialog dialog;

    private LoadingDialog loadingDialog;
    private MessageDialog messageDialog;

    private DatabaseReference databaseReference;

    private ComponentManager componentManager;

    private String id, description;

    public DescriptionFormDialog(Context context) {
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
        dialog.setContentView(R.layout.dialog_description_layout);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);

        ImageView imgClose = dialog.findViewById(R.id.imgClose);
        tvMessageTitle = dialog.findViewById(R.id.tvMessageTitle);
        etDescription = dialog.findViewById(R.id.etDescription);
        Button btnSubmit = dialog.findViewById(R.id.btnSubmit);

        loadingDialog = new LoadingDialog(context);
        messageDialog = new MessageDialog(context);
        componentManager = new ComponentManager(context);

        btnSubmit.setOnClickListener(view -> {
            if (componentManager.isNoInputError())
                submit();
        });

        imgClose.setOnClickListener(view -> dismissDialog());

        etDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                description = editable != null ? editable.toString() : "";

                componentManager.setInputRightDrawable(etDescription, !Credentials.isEmpty(description), Enums.CLEAR_TEXT);
            }
        });
    }

    private void setDialogWindow() {
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    public void showDialog() {
        dialog.show();

        tvMessageTitle.setText(context.getString(R.string.update_record, "Description"));
    }

    public void dismissDialog() {
        dialog.dismiss();

        etDescription.getText().clear();

        componentManager.hideInputErrors();
    }

    private void submit() {
        loadingDialog.showDialog();

        databaseReference.child(id).child("description")
                .setValue(Credentials.fullTrim(description)).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String msg = context.getString(R.string.update_record_success_msg, "the description");
                        submitSuccess(msg);
                    }  else if (task.getException() != null)
                        submitFailed(task.getException().toString());
                });
    }

    private void submitFailed(String errorMsg) {
        loadingDialog.dismissDialog();

        messageDialog.setMessageType(Enums.ERROR_MESSAGE);
        messageDialog.setMessage(errorMsg);
        messageDialog.showDialog();
    }

    private void submitSuccess(String msg) {
        loadingDialog.dismissDialog();
        dismissDialog();

        messageDialog.setMessageType(Enums.SUCCESS_MESSAGE);
        messageDialog.setMessage(msg);
        messageDialog.showDialog();
    }

    public void setDatabaseReference(DatabaseReference databaseReference) {
        this.databaseReference = databaseReference;
    }

    public void setIdAndDescription(String id, String string) {
        this.id = id;
        etDescription.setText(string);
    }
}
