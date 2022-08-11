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
import com.example.wsapandroidapp.Classes.Enums;
import com.example.wsapandroidapp.DataModel.Chapter;
import com.example.wsapandroidapp.DataModel.Supplier;
import com.example.wsapandroidapp.ImageActivity;
import com.example.wsapandroidapp.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Collections;
import java.util.List;

import androidx.constraintlayout.widget.ConstraintLayout;

public class SupplierInformationFormDialog {

    private ImageView imgIcon;
    private EditText etSupplier;
    private TextView tvMessageTitle, tvImageError, tvSupplierLabelError, tvChapter, tvChapterHint, tvChapterError;

    private final Context context;
    private Dialog dialog;

    private LoadingDialog loadingDialog;
    private MessageDialog messageDialog;
    private SupplierChapterDialog supplierChapterDialog;

    private FirebaseStorage firebaseStorage;

    private StorageReference storageReference;

    private DatabaseReference databaseReference;

    private ComponentManager componentManager;

    private Uri imageUri;
    private String supplierLabel;
    private Chapter selectedChapter;

    private Supplier supplier;
    private String previousImageUri;

    private boolean isImageChanged = false;

    public SupplierInformationFormDialog(Context context) {
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
        dialog.setContentView(R.layout.dialog_supplier_information_form_layout);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);

        ImageView imgClose = dialog.findViewById(R.id.imgClose);
        tvMessageTitle = dialog.findViewById(R.id.tvMessageTitle);
        imgIcon = dialog.findViewById(R.id.imgIcon);
        tvImageError = dialog.findViewById(R.id.tvImageError);
        etSupplier = dialog.findViewById(R.id.etSupplier);
        ConstraintLayout chapterLayout = dialog.findViewById(R.id.chapterLayout);
        tvChapter = dialog.findViewById(R.id.tvChapter);
        tvChapterHint = dialog.findViewById(R.id.tvChapterHint);
        tvChapterError = dialog.findViewById(R.id.tvChapterError);
        tvSupplierLabelError = dialog.findViewById(R.id.tvSupplierLabelError);
        Button btnChooseImage = dialog.findViewById(R.id.btnChooseImage);
        Button btnSubmit = dialog.findViewById(R.id.btnSubmit);

        loadingDialog = new LoadingDialog(context);
        messageDialog = new MessageDialog(context);
        supplierChapterDialog = new SupplierChapterDialog(context);

        supplierChapterDialog.setDialogListener(chapter -> {
            if (chapter != null) {
                selectedChapter = chapter;
                tvChapter.setText(chapter.getChapter());
                tvChapter.setVisibility(View.VISIBLE);
                tvChapterHint.setVisibility(View.GONE);
            }

            checkChapter();

            supplierChapterDialog.dismissDialog();
        });

        firebaseStorage = FirebaseStorage.getInstance();

        List<TextView> errorTextViewList =
                Collections.singletonList(tvSupplierLabelError);
        List<EditText> errorEditTextList =
                Collections.singletonList(etSupplier);

        componentManager = new ComponentManager(context);
        componentManager.initializeErrorComponents(errorTextViewList, errorEditTextList);

        imgIcon.setOnClickListener(view -> {
            if (supplier.getId() != null) {
                Intent intent = new Intent(context, ImageActivity.class);
                intent.putExtra("image", supplier.getImage());
                context.startActivity(intent);
            }
        });

        btnChooseImage.setOnClickListener(view -> {
            if (dialogListener != null) dialogListener.chooseImage();
        });

        btnSubmit.setOnClickListener(view -> {
            checkImage();
            checkLabel(supplierLabel, true, context.getString(R.string.supplier_label), tvSupplierLabelError, etSupplier);
            checkChapter();

            if (componentManager.isNoInputError() &&
                    imageUri != null && selectedChapter != null)
                submit();
        });

        imgClose.setOnClickListener(view -> dismissDialog());

        etSupplier.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                supplierLabel = editable != null ? editable.toString() : "";

                componentManager.setInputRightDrawable(etSupplier, !Credentials.isEmpty(supplierLabel), Enums.CLEAR_TEXT);
                checkLabel(supplierLabel, true, context.getString(R.string.category_label), tvSupplierLabelError, etSupplier);
            }
        });

        chapterLayout.setOnClickListener(view -> supplierChapterDialog.showDialog());
    }

    private void setDialogWindow() {
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    public void showDialog() {
        dialog.show();

        isImageChanged = false;

        tvMessageTitle.setText(context.getString(R.string.update_record, "Supplier Information"));
    }

    public void dismissDialog() {
        dialog.dismiss();

        imgIcon.setImageResource(R.drawable.ic_image_primary);
        imageUri = null;
        etSupplier.getText().clear();
        clearChapter();

        componentManager.hideInputErrors();
        componentManager.hideInputError(tvImageError);
        componentManager.hideInputError(tvChapterError);
    }

    private void clearChapter() {
        selectedChapter = null;
        tvChapter.setText(context.getString(R.string.chapter));
        tvChapter.setVisibility(View.GONE);
        tvChapterHint.setVisibility(View.VISIBLE);
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

    private void checkChapter() {
        componentManager.hideInputError(tvChapterError);

        if (selectedChapter == null)
            componentManager.showInputError(tvChapterError, context.getString(R.string.required_input_error, "Chapter"));
    }

    private String getFileExt(Uri uri) {
        ContentResolver contentResolver = context.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void submit() {
        loadingDialog.showDialog();

        if (isImageChanged) {
            if (previousImageUri != null)
                firebaseStorage.getReferenceFromUrl(previousImageUri).delete();

            storageReference.child(System.currentTimeMillis() + "-" + supplier.getId() + getFileExt(imageUri))
                    .putFile(imageUri).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (task.getResult() != null) {
                                task.getResult().getStorage().getDownloadUrl()
                                        .addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {
                                                if (task1.getResult() != null) {
                                                    supplier.setSupplier(Credentials.fullTrim(supplierLabel));
                                                    supplier.setImage(task1.getResult().toString());
                                                    supplier.setChapter(selectedChapter.getId());

                                                    databaseReference.child(supplier.getId())
                                                            .setValue(supplier).addOnCompleteListener(task11 -> {
                                                                if (task11.isSuccessful()) {
                                                                    String msg = context.getString(R.string.update_record_success_msg, "the supplier information");
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
            supplier.setSupplier(Credentials.fullTrim(supplierLabel));
            supplier.setChapter(selectedChapter.getId());

            databaseReference.child(supplier.getId())
                    .setValue(supplier).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String msg = context.getString(R.string.update_record_success_msg, "the supplier information");
                            submitSuccess(msg);
                        }  else if (task.getException() != null)
                            submitFailed(task.getException().toString());
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

    public void setImageData(Uri uri) {
        imageUri = uri;
        isImageChanged = true;

        Glide.with(context).load(imageUri).centerCrop().placeholder(R.drawable.ic_wsap).
                error(R.drawable.ic_wsap).into(imgIcon);
        checkImage();
    }

    public void setSupplier(Supplier supplier, Chapter chapter) {
        this.supplier = supplier;

        imageUri = Uri.parse(supplier.getImage());
        previousImageUri = supplier.getImage();

        Glide.with(context).load(supplier.getImage()).centerCrop().placeholder(R.drawable.ic_wsap).
                error(R.drawable.ic_wsap).into(imgIcon);
        checkImage();

        etSupplier.setText(supplier.getSupplier());

        if (chapter != null) {
            selectedChapter = chapter;
            tvChapter.setText(chapter.getChapter());
            tvChapter.setVisibility(View.VISIBLE);
            tvChapterHint.setVisibility(View.GONE);
        }

        checkChapter();
    }

    private DialogListener dialogListener;

    public interface DialogListener {
        void chooseImage();
    }

    public void setDialogListener(DialogListener dialogListener) {
        this.dialogListener = dialogListener;
    }
}
