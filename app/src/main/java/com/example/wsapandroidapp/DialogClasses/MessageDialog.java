package com.example.wsapandroidapp.DialogClasses;

import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.wsapandroidapp.Classes.Enums;
import com.example.wsapandroidapp.R;

public class MessageDialog {

    private TextView tvMessage;
    private ImageView imgIcon;

    private final Context context;
    private Dialog dialog;

    public MessageDialog(Context context) {
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
        dialog.setContentView(R.layout.dialog_message_layout);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);

        tvMessage = dialog.findViewById(R.id.tvMessage);
        imgIcon = dialog.findViewById(R.id.imgIcon);
        Button btnOK = dialog.findViewById(R.id.btnOK);

        btnOK.setOnClickListener(view -> dismissDialog());
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

    public void setMessage(String textCaption) {
        tvMessage.setText(textCaption);
    }

    public void setMessageType(int messageType) {
        if (messageType == Enums.INFO_MESSAGE) {
            imgIcon.setImageResource(messageType);
            imgIcon.setImageTintList(ColorStateList.valueOf(context.getColor(R.color.primary)));
        } else if (messageType == Enums.SUCCESS_MESSAGE) {
            imgIcon.setImageResource(messageType);
            imgIcon.setImageTintList(ColorStateList.valueOf(context.getColor(R.color.green)));
        } else if (messageType == Enums.ERROR_MESSAGE) {
            imgIcon.setImageResource(messageType);
            imgIcon.setImageTintList(ColorStateList.valueOf(context.getColor(R.color.red)));
        }
    }
}
