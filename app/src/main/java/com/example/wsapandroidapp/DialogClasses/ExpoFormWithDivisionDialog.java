package com.example.wsapandroidapp.DialogClasses;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.wsapandroidapp.Classes.ComponentManager;
import com.example.wsapandroidapp.Classes.Credentials;
import com.example.wsapandroidapp.Classes.DateTime;
import com.example.wsapandroidapp.Classes.Enums;
import com.example.wsapandroidapp.DataModel.Chapter;
import com.example.wsapandroidapp.DataModel.DateTimeRange;
import com.example.wsapandroidapp.DataModel.Division;
import com.example.wsapandroidapp.DataModel.Expo;
import com.example.wsapandroidapp.ImageActivity;
import com.example.wsapandroidapp.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Collections;
import java.util.List;

import androidx.constraintlayout.widget.ConstraintLayout;

public class ExpoFormWithDivisionDialog {

    private ImageView imgIcon;
    private EditText etExpoTitle, etDescription;
    private TextView tvDivision, tvDivisionHint, tvDivisionError;
    private TextView tvMessageTitle, tvImageError, tvExpoTitleError;
    private TextView tvStartDate, tvStartDateHint, tvStartDateError;
    private TextView tvEndDate, tvEndDateHint, tvEndDateError;
    private Button btnSubmit;

    private final Context context;
    private Dialog dialog;

    private LoadingDialog loadingDialog;
    private MessageDialog messageDialog;
    private ExpoDivisionDialog expoDivisionDialog;

    private FirebaseStorage firebaseStorage;

    private StorageReference storageReference;

    private DatabaseReference databaseReference;
    private boolean isUpdateMode = false;

    private ComponentManager componentManager;

    private Uri imageUri;
    private String expoTitle, description, startDate, endDate;
    private long startDateTime, endDateTime;
    private Division selectedDivision;

    private Expo expo;
    private String previousImageUri;

    private boolean isImageChanged = false;

    public ExpoFormWithDivisionDialog(Context context) {
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
        dialog.setContentView(R.layout.dialog_expo_form_with_division_layout);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);

        ImageView imgClose = dialog.findViewById(R.id.imgClose);
        tvMessageTitle = dialog.findViewById(R.id.tvMessageTitle);
        imgIcon = dialog.findViewById(R.id.imgIcon);
        tvImageError = dialog.findViewById(R.id.tvImageError);
        etExpoTitle = dialog.findViewById(R.id.etExpoTitle);
        etDescription = dialog.findViewById(R.id.etDescription);
        ConstraintLayout divisionLayout = dialog.findViewById(R.id.divisionLayout);
        tvDivision = dialog.findViewById(R.id.tvDivision);
        tvDivisionHint = dialog.findViewById(R.id.tvDivisionHint);
        tvDivisionError = dialog.findViewById(R.id.tvDivisionError);
        tvExpoTitleError = dialog.findViewById(R.id.tvExpoTitleError);
        ConstraintLayout startDateLayout = dialog.findViewById(R.id.startDateLayout);
        tvStartDate = dialog.findViewById(R.id.tvStartDate);
        tvStartDateHint = dialog.findViewById(R.id.tvStartDateHint);
        tvStartDateError = dialog.findViewById(R.id.tvStartDateError);
        ConstraintLayout endDateLayout = dialog.findViewById(R.id.endDateLayout);
        tvEndDate = dialog.findViewById(R.id.tvEndDate);
        tvEndDateHint = dialog.findViewById(R.id.tvEndDateHint);
        tvEndDateError = dialog.findViewById(R.id.tvEndDateError);
        tvExpoTitleError = dialog.findViewById(R.id.tvExpoTitleError);
        Button btnChooseImage = dialog.findViewById(R.id.btnChooseImage);
        btnSubmit = dialog.findViewById(R.id.btnSubmit);

        loadingDialog = new LoadingDialog(context);
        messageDialog = new MessageDialog(context);
        expoDivisionDialog = new ExpoDivisionDialog(context);

        expoDivisionDialog.setDialogListener(division -> {
            if (division != null) {
                selectedDivision = division;
                tvDivision.setText(division.getDivision());
                tvDivision.setVisibility(View.VISIBLE);
                tvDivisionHint.setVisibility(View.GONE);
            }

            checkDivision();

            expoDivisionDialog.dismissDialog();
        });

        firebaseStorage = FirebaseStorage.getInstance();

        List<TextView> errorTextViewList =
                Collections.singletonList(tvExpoTitleError);
        List<EditText> errorEditTextList =
                Collections.singletonList(etExpoTitle);

        componentManager = new ComponentManager(context);
        componentManager.initializeErrorComponents(errorTextViewList, errorEditTextList);

        imgIcon.setOnClickListener(view -> {
            if (expo.getId() != null) {
                Intent intent = new Intent(context, ImageActivity.class);
                intent.putExtra("image", expo.getImage());
                context.startActivity(intent);
            }
        });

        btnChooseImage.setOnClickListener(view -> {
            if (dialogListener != null) dialogListener.chooseImage();
        });

        btnSubmit.setOnClickListener(view -> {
            checkImage();
            checkTitle(expoTitle, true, context.getString(R.string.expo_title), tvExpoTitleError, etExpoTitle);
            checkDate(startDate, true, context.getString(R.string.start_date), tvStartDateError);
            checkDate(endDate, true, context.getString(R.string.end_date), tvEndDateError);
            checkDivision();

            if (componentManager.isNoInputError() && imageUri != null &&
                startDate != null && endDate != null && selectedDivision != null)
                submit();
        });

        imgClose.setOnClickListener(view -> dismissDialog());

        etExpoTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                expoTitle = editable != null ? editable.toString() : "";

                componentManager.setInputRightDrawable(etExpoTitle, !Credentials.isEmpty(expoTitle), Enums.CLEAR_TEXT);
                checkTitle(expoTitle, true, context.getString(R.string.expo_title), tvExpoTitleError, etExpoTitle);
            }
        });

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

        componentManager.setDatePickerListener(new ComponentManager.DatePickerListener() {
            @Override
            public void onSelect(long dateTime, EditText targetEditText) {

            }

            @Override
            public void onSelect(String date, EditText targetEditText) {

            }

            @Override
            public void onSelect(long dateTime, TextView targetTextView) {
                if (targetTextView == tvStartDate)
                    startDateTime = dateTime;
                else if (targetTextView == tvEndDate)
                    endDateTime = dateTime;
            }

            @Override
            public void onSelect(String date, TextView targetTextView) {
                targetTextView.setText(date);
                targetTextView.setVisibility(View.VISIBLE);

                if (targetTextView == tvStartDate) {
                    startDate = date;
                    tvStartDateHint.setVisibility(View.GONE);

                    checkDate(startDate, true, context.getString(R.string.start_date), tvStartDateError);
                } else if (targetTextView == tvEndDate) {
                    endDate = date;
                    tvEndDateHint.setVisibility(View.GONE);

                    checkDate(endDate, true, context.getString(R.string.end_date), tvEndDateError);
                }
            }
        });

        divisionLayout.setOnClickListener(view -> expoDivisionDialog.showDialog());

        startDateLayout.setOnClickListener(view -> componentManager.showDatePickerDialog(tvStartDate));

        endDateLayout.setOnClickListener(view -> componentManager.showDatePickerDialog(tvEndDate));
    }

    private void setDialogWindow() {
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    public void showDialog() {
        dialog.show();

        isImageChanged = false;

        tvMessageTitle.setText(context.getString(R.string.add_record, "Expo"));
        btnSubmit.setText(context.getString(R.string.add));
        if (isUpdateMode) {
            tvMessageTitle.setText(context.getString(R.string.update_record, "Expo"));
            btnSubmit.setText(context.getString(R.string.update));
        } else expo = new Expo();
    }

    public void dismissDialog() {
        dialog.dismiss();

        imgIcon.setImageResource(R.drawable.ic_image_primary);
        imageUri = null;
        etExpoTitle.getText().clear();
        etDescription.getText().clear();
        clearStartDate();
        clearEndDate();
        clearDivision();

        componentManager.hideInputErrors();
        componentManager.hideInputError(tvImageError);
        componentManager.hideInputError(tvStartDateError);
        componentManager.hideInputError(tvEndDateError);
        componentManager.hideInputError(tvDivisionError);
    }

    public void setDivisions(List<Division> divisions) {
        expoDivisionDialog.setDivisions(divisions);
    }

    private void clearStartDate() {
        startDateTime = 0;
        tvStartDate.setText(context.getString(R.string.start_date));
        tvStartDate.setVisibility(View.GONE);
        tvStartDateHint.setVisibility(View.VISIBLE);
    }

    private void clearEndDate() {
        endDateTime = 0;
        tvEndDate.setText(context.getString(R.string.end_date));
        tvEndDate.setVisibility(View.GONE);
        tvEndDateHint.setVisibility(View.VISIBLE);
    }

    private void clearDivision() {
        selectedDivision = null;
        tvDivision.setText(context.getString(R.string.division));
        tvDivision.setVisibility(View.GONE);
        tvDivisionHint.setVisibility(View.VISIBLE);
    }

    private void checkImage() {
        componentManager.hideInputError(tvImageError);

        if (imageUri == null)
            componentManager.showInputError(tvImageError, context.getString(R.string.required_input_error, "Image"));
    }

    private void checkTitle(String string, boolean isRequired, String fieldName,
                            TextView targetTextView, EditText targetEditText) {
        componentManager.hideInputError(targetTextView, targetEditText);

        if (Credentials.isEmpty(string) && isRequired)
            componentManager.showInputError(targetTextView,
                    context.getString(R.string.required_input_error, fieldName),
                    targetEditText);
        else if (!Credentials.isValidLength(string, Credentials.REQUIRED_LABEL_LENGTH, 0))
            componentManager.showInputError(targetTextView,
                    context.getString(R.string.length_error, fieldName, Credentials.REQUIRED_LABEL_LENGTH),
                    targetEditText);
    }

    private void checkDate(String string, boolean isRequired, String fieldName, TextView targetTextView) {
        componentManager.hideInputError(targetTextView);

        if (Credentials.isEmpty(string) && isRequired)
            componentManager.showInputError(targetTextView, context.getString(R.string.required_input_error, fieldName));
    }

    private void checkDivision() {
        componentManager.hideInputError(tvDivisionError);

        if (selectedDivision == null)
            componentManager.showInputError(tvDivisionError, context.getString(R.string.required_input_error, "Division"));
    }

    private String getFileExt(Uri uri) {
        ContentResolver contentResolver = context.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void submit() {
        loadingDialog.showDialog();

        DateTimeRange dateTimeRange = new DateTimeRange(startDateTime, endDateTime);

        String id = Credentials.getUniqueId();
        if (isUpdateMode) id = expo.getId();

        if (isImageChanged) {
            String finalId = id;

            if (previousImageUri != null)
                firebaseStorage.getReferenceFromUrl(previousImageUri).delete();

            storageReference.child(System.currentTimeMillis() + "-" + id + getFileExt(imageUri))
                    .putFile(imageUri).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (task.getResult() != null) {
                                task.getResult().getStorage().getDownloadUrl()
                                        .addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {
                                                if (task1.getResult() != null) {
                                                    if (isUpdateMode) {
                                                        expo.setExpo(Credentials.fullTrim(expoTitle));
                                                        expo.setDescription(Credentials.fullTrim(description));
                                                        expo.setImage(task1.getResult().toString());
                                                        expo.setDateTimeRange(dateTimeRange);
                                                    } else expo =
                                                        new Expo(
                                                                finalId, Credentials.fullTrim(expoTitle),
                                                                Credentials.fullTrim(description),
                                                                task1.getResult().toString(), selectedDivision.getId(),
                                                                dateTimeRange
                                                        );

                                                    databaseReference.child(expo.getId())
                                                            .setValue(expo).addOnCompleteListener(task11 -> {
                                                                if (task11.isSuccessful()) {
                                                                    String msg = context.getString(R.string.add_record_success_msg, "a expo");
                                                                    if (isUpdateMode) msg = context.getString(R.string.update_record_success_msg, "the expo");
                                                                    submitSuccess(msg);
                                                                }  else if (task11.getException() != null)
                                                                    submitFailed(task11.getException().toString());
                                                            });
                                                }
                                            }  else if (task1.getException() != null)
                                                submitFailed(task1.getException().toString());
                                        });
                            }
                        }  else if (task.getException() != null)
                            submitFailed(task.getException().toString());
                    });
        } else {
            expo.setExpo(Credentials.fullTrim(expoTitle));
            expo.setDescription(Credentials.fullTrim(description));
            expo.setDateTimeRange(dateTimeRange);

            databaseReference.child(expo.getId())
                    .setValue(expo).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String msg = context.getString(R.string.add_record_success_msg, "a expo");
                            if (isUpdateMode) msg = context.getString(R.string.update_record_success_msg, "the expo");
                            submitSuccess(msg);
                        }  else if (task.getException() != null)
                            submitFailed(task.getException().toString());
                    });
        }
    }

    public void deleteExpo(Expo expo, boolean isDeleteImage) {
        loadingDialog.showDialog();

        if (isDeleteImage) {
            firebaseStorage.getReferenceFromUrl(expo.getImage())
                    .delete().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            databaseReference.child(expo.getId()).removeValue()
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            String msg = context.getString(R.string.delete_record_success_msg, "the expo");
                                            submitSuccess(msg);
                                        }  else if (task1.getException() != null)
                                            submitFailed(task1.getException().toString());
                                    });
                        }  else if (task.getException() != null)
                            submitFailed(task.getException().toString());
                    });
        } else {
            databaseReference.child(expo.getId()).removeValue()
                    .addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            String msg = context.getString(R.string.delete_record_success_msg, "the expo");
                            submitSuccess(msg);
                        }  else if (task1.getException() != null)
                            submitFailed(task1.getException().toString());
                    });
        }
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

    public void setDirectory(String directory) {
        storageReference = firebaseStorage.getReference(directory);
    }

    public void setDatabaseReference(DatabaseReference databaseReference) {
        this.databaseReference = databaseReference;
    }

    public void setUpdateMode(boolean isUpdateMode) {
        this.isUpdateMode = isUpdateMode;
    }

    public void setImageData(Uri uri) {
        imageUri = uri;
        isImageChanged = true;

        Glide.with(context).load(imageUri).centerCrop().placeholder(R.drawable.ic_wsap).
                error(R.drawable.ic_wsap).into(imgIcon);
        checkImage();
    }

    public void setExpo(Expo expo, Division division) {
        this.expo = expo;

        imageUri = Uri.parse(expo.getImage());
        previousImageUri = expo.getImage();

        Glide.with(context).load(expo.getImage()).centerCrop().placeholder(R.drawable.ic_wsap).
                error(R.drawable.ic_wsap).into(imgIcon);
        checkImage();

        etExpoTitle.setText(expo.getExpo());
        etDescription.setText(expo.getDescription());

        startDateTime = expo.getDateTimeRange().getStartDateTime();
        endDateTime = expo.getDateTimeRange().getEndDateTime();

        startDate = new DateTime(startDateTime).getDateText();
        endDate = new DateTime(endDateTime).getDateText();

        tvStartDate.setText(startDate);
        tvStartDate.setVisibility(View.VISIBLE);
        tvStartDateHint.setVisibility(View.GONE);

        checkDate(startDate, true, context.getString(R.string.start_date), tvStartDateError);

        tvEndDate.setText(endDate);
        tvEndDate.setVisibility(View.VISIBLE);
        tvEndDateHint.setVisibility(View.GONE);

        checkDate(endDate, true, context.getString(R.string.end_date), tvEndDateError);

        if (division != null) {
            selectedDivision = division;
            tvDivisionHint.setText(division.getDivision());
            tvDivision.setVisibility(View.VISIBLE);
            tvDivisionHint.setVisibility(View.GONE);
        }

        checkDivision();
    }

    private DialogListener dialogListener;

    public interface DialogListener {
        void chooseImage();
    }

    public void setDialogListener(DialogListener dialogListener) {
        this.dialogListener = dialogListener;
    }
}
