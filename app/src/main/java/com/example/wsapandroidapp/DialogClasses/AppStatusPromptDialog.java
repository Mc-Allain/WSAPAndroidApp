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

public class AppStatusPromptDialog {

    private TextView tvMessageTitle;

    private final Context context;
    private Dialog dialog;

    public AppStatusPromptDialog(Context context) {
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
        dialog.setContentView(R.layout.dialog_app_status_prompt_layout);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);

        tvMessageTitle = dialog.findViewById(R.id.tvMessageTitle);
        ImageView imgClose = dialog.findViewById(R.id.imgClose);
        Button btnUpdate = dialog.findViewById(R.id.btnUpdate);

        btnUpdate.setOnClickListener(view -> {
            if (dialogListener != null) dialogListener.onExit();
        });

        imgClose.setOnClickListener(view -> {
            if (dialogListener != null) dialogListener.onExit();
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

    public void setTitle(String status) {
        tvMessageTitle.setText(context.getString(R.string.app_status_title, status));
    }

    DialogListener dialogListener;

    public interface DialogListener {
        void onExit();
    }

    public void setDialogListener(DialogListener dialogListener) {
        this.dialogListener = dialogListener;
    }
}
