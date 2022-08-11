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
import com.example.wsapandroidapp.DataModel.SocialMedia;
import com.example.wsapandroidapp.R;

import java.util.Arrays;
import java.util.List;

public class SocialMediaFormDialog {

    private EditText etFacebook, etInstagram, etTwitter, etYouTube, etWebsite;
    private TextView tvFacebookError, tvInstagramError, tvTwitterError, tvYouTubeError, tvWebsiteError;

    private final Context context;
    private Dialog dialog;

    private ComponentManager componentManager;

    private String facebook = "", instagram = "", twitter = "", youtube = "", website = "";

    private SocialMedia socialMedia;

    public SocialMediaFormDialog(Context context) {
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
        dialog.setContentView(R.layout.dialog_social_media_layout);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);

        ImageView imgClose = dialog.findViewById(R.id.imgClose);
        etFacebook = dialog.findViewById(R.id.etFacebook);
        etInstagram = dialog.findViewById(R.id.etInstagram);
        etTwitter = dialog.findViewById(R.id.etTwitter);
        etYouTube = dialog.findViewById(R.id.etYouTube);
        etWebsite = dialog.findViewById(R.id.etWebsite);
        tvFacebookError = dialog.findViewById(R.id.tvFacebookError);
        tvInstagramError = dialog.findViewById(R.id.tvInstagramError);
        tvTwitterError = dialog.findViewById(R.id.tvTwitterError);
        tvYouTubeError = dialog.findViewById(R.id.tvYouTubeError);
        tvWebsiteError = dialog.findViewById(R.id.tvWebsiteError);
        Button btnSubmit = dialog.findViewById(R.id.btnSubmit);

        List<TextView> errorTextViewList =
                Arrays.asList(tvFacebookError, tvInstagramError, tvTwitterError, tvYouTubeError, tvWebsiteError);
        List<EditText> errorEditTextList =
                Arrays.asList(etFacebook, etInstagram, etTwitter, etYouTube, etWebsite);

        componentManager = new ComponentManager(context);
        componentManager.initializeErrorComponents(errorTextViewList, errorEditTextList);

        btnSubmit.setOnClickListener(view -> {
            socialMedia = new SocialMedia(
                    Credentials.fullTrim(facebook),
                    Credentials.fullTrim(instagram),
                    Credentials.fullTrim(twitter),
                    Credentials.fullTrim(youtube),
                    Credentials.fullTrim(website)
            );
            if (dialogListener != null) dialogListener.submit(socialMedia);
        });

        imgClose.setOnClickListener(view -> dismissDialog());

        etFacebook.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                facebook = editable != null ? editable.toString() : "";

                componentManager.setInputRightDrawable(etFacebook, !Credentials.isEmpty(facebook), Enums.CLEAR_TEXT);
                checkLink(facebook, false, context.getString(R.string.facebook), tvFacebookError, etFacebook);
            }
        });

        etInstagram.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                instagram = editable != null ? editable.toString() : "";

                componentManager.setInputRightDrawable(etInstagram, !Credentials.isEmpty(instagram), Enums.CLEAR_TEXT);
                checkLink(instagram, false, context.getString(R.string.instagram), tvInstagramError, etInstagram);
            }
        });

        etTwitter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                twitter = editable != null ? editable.toString() : "";

                componentManager.setInputRightDrawable(etTwitter, !Credentials.isEmpty(twitter), Enums.CLEAR_TEXT);
                checkLink(twitter, false, context.getString(R.string.trivias), tvTwitterError, etTwitter);
            }
        });

        etYouTube.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                youtube = editable != null ? editable.toString() : "";

                componentManager.setInputRightDrawable(etYouTube, !Credentials.isEmpty(youtube), Enums.CLEAR_TEXT);
                checkLink(youtube, false, context.getString(R.string.youtube), tvYouTubeError, etYouTube);
            }
        });

        etWebsite.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                website = editable != null ? editable.toString() : "";

                componentManager.setInputRightDrawable(etWebsite, !Credentials.isEmpty(website), Enums.CLEAR_TEXT);
                checkLink(website, false, context.getString(R.string.website), tvWebsiteError, etWebsite);
            }
        });
    }

    private void setDialogWindow() {
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    public void showDialog() {
        dialog.show();

        if (socialMedia != null) {
            etFacebook.setText(socialMedia.getFacebook());
            etInstagram.setText(socialMedia.getInstagram());
            etTwitter.setText(socialMedia.getTwitter());
            etYouTube.setText(socialMedia.getYoutube());
            etWebsite.setText(socialMedia.getWebsite());
        }
    }

    public void dismissDialog() {
        dialog.dismiss();

        etFacebook.getText().clear();
        etInstagram.getText().clear();
        etTwitter.getText().clear();
        etYouTube.getText().clear();
        etWebsite.getText().clear();
    }

    private void checkLink(String string, boolean isRequired, String fieldName,
                           TextView targetTextView, EditText targetEditText) {
        componentManager.hideInputError(targetTextView, targetEditText);

        if (Credentials.isEmpty(string) && isRequired)
            componentManager.showInputError(targetTextView,
                    context.getString(R.string.required_input_error, fieldName),
                    targetEditText);
        else if (!Credentials.isEmpty(string) && !Credentials.isValidLink(string))
            componentManager.showInputError(targetTextView,
                    context.getString(R.string.invalid_error, fieldName + " Link"),
                    targetEditText);
    }

    public void setSocialMedia(SocialMedia socialMedia) {
        this.socialMedia = socialMedia;
    }

    private DialogListener dialogListener;

    public interface DialogListener {
        void submit(SocialMedia socialMedia);
    }

    public void setDialogListener(DialogListener dialogListener) {
        this.dialogListener = dialogListener;
    }
}
