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
import com.example.wsapandroidapp.DataModel.ContactInfo;
import com.example.wsapandroidapp.R;

import java.util.Arrays;
import java.util.List;

public class ContactInformationFormDialog {

    private EditText etPhoneNumber, etEmailAddress, etLocationAddress;
    private TextView tvPhoneNumberError, tvEmailAddressError;

    private final Context context;
    private Dialog dialog;

    private ComponentManager componentManager;

    private String phoneNumber = "", emailAddress = "", locationAddress = "";

    private ContactInfo contactInfo;

    public ContactInformationFormDialog(Context context) {
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
        dialog.setContentView(R.layout.dialog_contact_information_form_layout);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);

        ImageView imgClose = dialog.findViewById(R.id.imgClose);
        etPhoneNumber = dialog.findViewById(R.id.etPhoneNumber);
        etEmailAddress = dialog.findViewById(R.id.etEmailAddress);
        etLocationAddress = dialog.findViewById(R.id.etLocationAddress);
        tvPhoneNumberError = dialog.findViewById(R.id.tvPhoneNumberError);
        tvEmailAddressError = dialog.findViewById(R.id.tvEmailAddressError);
        Button btnSubmit = dialog.findViewById(R.id.btnSubmit);

        List<TextView> errorTextViewList =
                Arrays.asList(tvPhoneNumberError, tvEmailAddressError);
        List<EditText> errorEditTextList =
                Arrays.asList(etPhoneNumber, etEmailAddress);

        componentManager = new ComponentManager(context);
        componentManager.initializeErrorComponents(errorTextViewList, errorEditTextList);

        btnSubmit.setOnClickListener(view -> {
            checkPhoneNumberError();
            checkEmailAddressError();

            if (componentManager.isNoInputError()) {
                long parsedPhoneNumber = 0;
                if (!Credentials.isEmpty(phoneNumber)) parsedPhoneNumber = Long.parseLong(phoneNumber);

                contactInfo = new ContactInfo(emailAddress, Credentials.fullTrim(locationAddress), parsedPhoneNumber);
                if (dialogListener != null) dialogListener.submit(contactInfo);
            }
        });

        imgClose.setOnClickListener(view -> dismissDialog());

        etPhoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                phoneNumber = editable != null ? editable.toString() : "";

                componentManager.setInputRightDrawable(etPhoneNumber, !Credentials.isEmpty(phoneNumber), Enums.CLEAR_TEXT);
                checkPhoneNumberError();
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
                emailAddress = editable != null ? editable.toString() : "";

                componentManager.setInputRightDrawable(etEmailAddress, !Credentials.isEmpty(emailAddress), Enums.CLEAR_TEXT);
                checkEmailAddressError();
            }
        });

        etLocationAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                locationAddress = editable != null ? editable.toString() : "";

                componentManager.setInputRightDrawable(etLocationAddress, !Credentials.isEmpty(locationAddress), Enums.CLEAR_TEXT);
            }
        });
    }

    private void setDialogWindow() {
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    public void showDialog() {
        dialog.show();

        if (contactInfo != null) {
            phoneNumber = "";
            if (String.valueOf(contactInfo.getPhoneNumber()).length() > 0 &&
                    contactInfo.getPhoneNumber() != 0)
                phoneNumber = String.valueOf(contactInfo.getPhoneNumber());

            etPhoneNumber.setText(phoneNumber);
            etEmailAddress.setText(contactInfo.getEmailAddress());
            etLocationAddress.setText(contactInfo.getLocationAddress());
        }
    }

    public void dismissDialog() {
        dialog.dismiss();

        contactInfo = null;
        etPhoneNumber.getText().clear();
        etEmailAddress.getText().clear();
        etLocationAddress.getText().clear();

        componentManager.hideInputErrors();
    }

    private void checkPhoneNumberError() {
        componentManager.hideInputError(tvPhoneNumberError, etPhoneNumber);

        if (!Credentials.isEmpty(phoneNumber) &&
                !Credentials.isValidPhoneNumber(phoneNumber,
                        Credentials.REQUIRED_PHONE_NUMBER_LENGTH_PH_WITH_PREFIX,
                        Credentials.REQUIRED_PHONE_NUMBER_LENGTH_PH))
            componentManager.showInputError(tvPhoneNumberError,
                    context.getString(R.string.invalid_error, context.getString(R.string.phone_number)),
                    etPhoneNumber);
    }

    private void checkEmailAddressError() {
        componentManager.hideInputError(tvEmailAddressError, etEmailAddress);

        if (!Credentials.isEmpty(emailAddress) && !Credentials.isValidEmailAddress(emailAddress))
            componentManager.showInputError(tvEmailAddressError,
                    context.getString(R.string.invalid_error, context.getString(R.string.email_address)),
                    etEmailAddress);
    }

    public void setContactInfo(ContactInfo contactInfo) {
        this.contactInfo = contactInfo;
    }

    private DialogListener dialogListener;

    public interface DialogListener {
        void submit(ContactInfo contactInfo);
    }

    public void setDialogListener(DialogListener dialogListener) {
        this.dialogListener = dialogListener;
    }
}
