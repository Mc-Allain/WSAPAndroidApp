package com.example.wsapandroidapp.DialogClasses;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.example.wsapandroidapp.R;

public class UserRoleDialog {

    private RadioGroup radioGroup;

    private final Context context;
    private Dialog dialog;

    private int currentSelectedIndex;

    public UserRoleDialog(Context context) {
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
        dialog.setContentView(R.layout.dialog_user_role_layout);

        radioGroup = dialog.findViewById(R.id.radioGroup);
        Button btnConfirm = dialog.findViewById(R.id.btnConfirm);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);

        btnConfirm.setOnClickListener(view -> {
            if (dialogListener != null) dialogListener.onSelect(currentSelectedIndex);
        });

        btnCancel.setOnClickListener(view -> dismissDialog());

        for (int i = 0; i < radioGroup.getChildCount(); i++) {
            int selectedIndex = i;
            radioGroup.getChildAt(i).setOnClickListener(view -> currentSelectedIndex = selectedIndex);
        }
    }

    private void setDialogWindow() {
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    public void showDialog() {
        dialog.show();

        RadioButton radioButton = (RadioButton) radioGroup.getChildAt(currentSelectedIndex);
        radioButton.setChecked(true);
    }

    public void dismissDialog() {
        dialog.dismiss();
    }

    public void setCurrentSelectedIndex(int currentSelectedIndex) {
        this.currentSelectedIndex = currentSelectedIndex;
    }

    private DialogListener dialogListener;

    public interface DialogListener {
        void onSelect(int currentSelectedIndex);
    }

    public void setDialogListener(DialogListener dialogListener) {
        this.dialogListener = dialogListener;
    }
}
