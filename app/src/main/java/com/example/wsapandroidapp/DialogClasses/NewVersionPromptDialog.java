package com.example.wsapandroidapp.DialogClasses;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.wsapandroidapp.R;

public class NewVersionPromptDialog {

    TextView tvVersions;

    private final Context context;
    private Dialog dialog;

    public NewVersionPromptDialog(Context context) {
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
        dialog.setContentView(R.layout.dialog_new_version_prompt_layout);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);

        ImageView imgClose = dialog.findViewById(R.id.imgClose);
        tvVersions = dialog.findViewById(R.id.tvVersions);
        Button btnUpdate = dialog.findViewById(R.id.btnUpdate);

        btnUpdate.setOnClickListener(view -> {
            if (dialogListener != null) dialogListener.onUpdate();
        });

        imgClose.setOnClickListener(view -> {
            if (dialogListener != null) dialogListener.onCancel();
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

    public void setVersion(double currentVersion, double latestVersion) {
        String version = context.getString(R.string.current_version_italicized, currentVersion) + "\n" +
                context.getString(R.string.latest_version_italicized, latestVersion);
        tvVersions.setText(version);
    }

    private DialogListener dialogListener;

    public interface DialogListener {
        void onUpdate();
        void onCancel();
    }

    public void setDialogListener(DialogListener dialogListener) {
        this.dialogListener = dialogListener;
    }
}
