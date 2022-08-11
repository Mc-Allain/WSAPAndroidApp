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
import com.example.wsapandroidapp.Classes.Enums;
import com.example.wsapandroidapp.DataModel.Exhibitor;
import com.example.wsapandroidapp.ImageActivity;
import com.example.wsapandroidapp.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Collections;
import java.util.List;

public class ExhibitorFormDialog {

    private ImageView imgIcon;
    private EditText etExhibitor, etDescription;
    private TextView tvMessageTitle, tvImageError, tvExhibitorLabelError;
    private Button btnSubmit;

    private final Context context;
    private Dialog dialog;

    private LoadingDialog loadingDialog;
    private MessageDialog messageDialog;

    private FirebaseStorage firebaseStorage;

    private StorageReference storageReference;

    private DatabaseReference databaseReference;
    private boolean isUpdateMode = false;

    private ComponentManager componentManager;

    private Uri imageUri;
    private String exhibitorLabel, description;
    private String categoryId;

    private Exhibitor exhibitor;
    private String previousImageUri;

    private boolean isImageChanged = false;

    public ExhibitorFormDialog(Context context) {
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
        dialog.setContentView(R.layout.dialog_exhibitor_form_layout);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);

        ImageView imgClose = dialog.findViewById(R.id.imgClose);
        tvMessageTitle = dialog.findViewById(R.id.tvMessageTitle);
        imgIcon = dialog.findViewById(R.id.imgIcon);
        tvImageError = dialog.findViewById(R.id.tvImageError);
        etExhibitor = dialog.findViewById(R.id.etExhibitor);
        etDescription = dialog.findViewById(R.id.etDescription);
        tvExhibitorLabelError = dialog.findViewById(R.id.tvExhibitorLabelError);
        Button btnChooseImage = dialog.findViewById(R.id.btnChooseImage);
        btnSubmit = dialog.findViewById(R.id.btnSubmit);

        loadingDialog = new LoadingDialog(context);
        messageDialog = new MessageDialog(context);

        firebaseStorage = FirebaseStorage.getInstance();

        List<TextView> errorTextViewList =
                Collections.singletonList(tvExhibitorLabelError);
        List<EditText> errorEditTextList =
                Collections.singletonList(etExhibitor);

        componentManager = new ComponentManager(context);
        componentManager.initializeErrorComponents(errorTextViewList, errorEditTextList);

        imgIcon.setOnClickListener(view -> {
            if (exhibitor.getId() != null) {
                Intent intent = new Intent(context, ImageActivity.class);
                intent.putExtra("image", exhibitor.getImage());
                context.startActivity(intent);
            }
        });

        btnChooseImage.setOnClickListener(view -> {
            if (dialogListener != null) dialogListener.chooseImage();
        });

        btnSubmit.setOnClickListener(view -> {
            checkImage();
            checkLabel(exhibitorLabel, true, context.getString(R.string.exhibitor_label), tvExhibitorLabelError, etExhibitor);

            if (componentManager.isNoInputError() && imageUri != null)
                submit();
        });

        imgClose.setOnClickListener(view -> dismissDialog());

        etExhibitor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                exhibitorLabel = editable != null ? editable.toString() : "";

                componentManager.setInputRightDrawable(etExhibitor, !Credentials.isEmpty(exhibitorLabel), Enums.CLEAR_TEXT);
                checkLabel(exhibitorLabel, true, context.getString(R.string.exhibitor_label), tvExhibitorLabelError, etExhibitor);
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
    }

    private void setDialogWindow() {
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    public void showDialog() {
        dialog.show();

        isImageChanged = false;

        tvMessageTitle.setText(context.getString(R.string.add_record, "Exhibitor"));
        btnSubmit.setText(context.getString(R.string.add));
        if (isUpdateMode) {
            tvMessageTitle.setText(context.getString(R.string.update_record, "Exhibitor"));
            btnSubmit.setText(context.getString(R.string.update));
        } else exhibitor = new Exhibitor();
    }

    public void dismissDialog() {
        dialog.dismiss();

        imgIcon.setImageResource(R.drawable.ic_image_primary);
        imageUri = null;
        etExhibitor.getText().clear();
        etDescription.getText().clear();

        componentManager.hideInputErrors();
        componentManager.hideInputError(tvImageError);
    }

    private void checkImage() {
        componentManager.hideInputError(tvImageError);

        if (imageUri == null)
            componentManager.showInputError(tvImageError, context.getString(R.string.required_input_error, "Image"));
    }

    private void checkLabel(String string, boolean isRequired, String fieldName,
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

    private String getFileExt(Uri uri) {
        ContentResolver contentResolver = context.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void submit() {
        loadingDialog.showDialog();

        String id = Credentials.getUniqueId();
        if (isUpdateMode) id = exhibitor.getId();

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
                                                        exhibitor.setExhibitor(Credentials.fullTrim(exhibitorLabel));
                                                        exhibitor.setDescription(Credentials.fullTrim(description));
                                                        exhibitor.setImage(task1.getResult().toString());
                                                    } else exhibitor =
                                                        new Exhibitor(
                                                                finalId, Credentials.fullTrim(exhibitorLabel),
                                                                Credentials.fullTrim(description),
                                                                task1.getResult().toString(), categoryId
                                                        );

                                                    databaseReference.child(exhibitor.getId())
                                                            .setValue(exhibitor).addOnCompleteListener(task11 -> {
                                                                if (task11.isSuccessful()) {
                                                                    String msg = context.getString(R.string.add_record_success_msg, "a exhibitor");
                                                                    if (isUpdateMode) msg = context.getString(R.string.update_record_success_msg, "the exhibitor");
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
            exhibitor.setExhibitor(Credentials.fullTrim(exhibitorLabel));
            exhibitor.setDescription(Credentials.fullTrim(description));

            databaseReference.child(exhibitor.getId())
                    .setValue(exhibitor).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String msg = context.getString(R.string.add_record_success_msg, "a exhibitor");
                            if (isUpdateMode) msg = context.getString(R.string.update_record_success_msg, "the exhibitor");
                            submitSuccess(msg);
                        }  else if (task.getException() != null)
                            submitFailed(task.getException().toString());
                    });
        }
    }

    public void deleteExhibitor(Exhibitor exhibitor, boolean isDeleteImage) {
        loadingDialog.showDialog();

        if (isDeleteImage) {
            firebaseStorage.getReferenceFromUrl(exhibitor.getImage())
                    .delete().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            databaseReference.child(exhibitor.getId()).removeValue()
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            String msg = context.getString(R.string.delete_record_success_msg, "the exhibitor");
                                            submitSuccess(msg);
                                        }  else if (task1.getException() != null)
                                            submitFailed(task1.getException().toString());
                                    });
                        }  else if (task.getException() != null)
                            submitFailed(task.getException().toString());
                    });
        } else {
            databaseReference.child(exhibitor.getId()).removeValue()
                    .addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            String msg = context.getString(R.string.delete_record_success_msg, "the exhibitor");
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

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public void setExhibitor(Exhibitor exhibitor) {
        this.exhibitor = exhibitor;

        imageUri = Uri.parse(exhibitor.getImage());
        previousImageUri = exhibitor.getImage();

        Glide.with(context).load(exhibitor.getImage()).centerCrop().placeholder(R.drawable.ic_wsap).
                error(R.drawable.ic_wsap).into(imgIcon);
        checkImage();

        etExhibitor.setText(exhibitor.getExhibitor());
        etDescription.setText(exhibitor.getDescription());
    }

    private DialogListener dialogListener;

    public interface DialogListener {
        void chooseImage();
    }

    public void setDialogListener(DialogListener dialogListener) {
        this.dialogListener = dialogListener;
    }
}
