package com.example.wsapandroidapp.DialogClasses;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.wsapandroidapp.Classes.ComponentManager;
import com.example.wsapandroidapp.Classes.Credentials;
import com.example.wsapandroidapp.Classes.DateTime;
import com.example.wsapandroidapp.R;

import androidx.constraintlayout.widget.ConstraintLayout;

public class TargetWeddingDateFormDialog {

    private TextView tvTargetWeddingDate, tvTargetWeddingDateHint, tvTargetWeddingDateError;

    private final Context context;
    private Dialog dialog;

    private ComponentManager componentManager;

    private String targetWeddingDate;
    private long targetWeddingDateTime;

    public TargetWeddingDateFormDialog(Context context) {
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
        dialog.setContentView(R.layout.dialog_target_wedding_date_form_layout);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);

        ImageView imgClose = dialog.findViewById(R.id.imgClose);
        ConstraintLayout targetWeddingDateLayout = dialog.findViewById(R.id.targetWeddingDateLayout);
        tvTargetWeddingDate = dialog.findViewById(R.id.tvTargetWeddingDate);
        tvTargetWeddingDateHint = dialog.findViewById(R.id.tvTargetWeddingDateHint);
        tvTargetWeddingDateError = dialog.findViewById(R.id.tvTargetWeddingDateError);
        Button btnSubmit = dialog.findViewById(R.id.btnSubmit);

        componentManager = new ComponentManager(context);

        btnSubmit.setOnClickListener(view -> {
            checkDate(targetWeddingDate, true, context.getString(R.string.target_wedding_date), tvTargetWeddingDateError);

            if (componentManager.isNoInputError() && targetWeddingDate != null &&
                    dialogListener != null) dialogListener.onSubmit(targetWeddingDateTime);
        });

        imgClose.setOnClickListener(view -> {
            if (dialogListener != null) dialogListener.onCancel();
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
                if (targetTextView == tvTargetWeddingDate)
                    targetWeddingDateTime = dateTime;
            }

            @Override
            public void onSelect(String date, TextView targetTextView) {
                targetTextView.setText(date);
                targetTextView.setVisibility(View.VISIBLE);

                if (targetTextView == tvTargetWeddingDate) {
                    targetWeddingDate = date;
                    tvTargetWeddingDateHint.setVisibility(View.GONE);

                    checkDate(targetWeddingDate, true, context.getString(R.string.target_wedding_date), tvTargetWeddingDateError);
                }
            }
        });

        targetWeddingDateLayout.setOnClickListener(view -> componentManager.showDatePickerDialog(tvTargetWeddingDate));
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

        clearTargetWeddingDate();

        componentManager.hideInputErrors();
        componentManager.hideInputError(tvTargetWeddingDateError);
    }

    public void setTargetWeddingDate(long targetWeddingDateTime) {
        this.targetWeddingDateTime = targetWeddingDateTime;

        targetWeddingDate = new DateTime(targetWeddingDateTime).getDateText();

        tvTargetWeddingDate.setText(targetWeddingDate);
        tvTargetWeddingDate.setVisibility(View.VISIBLE);
        tvTargetWeddingDateHint.setVisibility(View.GONE);

        checkDate(targetWeddingDate, true, context.getString(R.string.target_wedding_date), tvTargetWeddingDateError);
    }

    private void clearTargetWeddingDate() {
        targetWeddingDateTime = 0;
        tvTargetWeddingDate.setText(context.getString(R.string.start_date));
        tvTargetWeddingDate.setVisibility(View.GONE);
        tvTargetWeddingDateHint.setVisibility(View.VISIBLE);
    }

    private void checkDate(String string, boolean isRequired, String fieldName, TextView targetTextView) {
        componentManager.hideInputError(targetTextView);

        if (Credentials.isEmpty(string) && isRequired)
            componentManager.showInputError(targetTextView, context.getString(R.string.required_input_error, fieldName));
    }

    private DialogListener dialogListener;

    public interface DialogListener {
        void onSubmit(long targetWeddingDateTime);
        void onCancel();
    }

    public void setDialogListener(DialogListener dialogListener) {
        this.dialogListener = dialogListener;
    }
}
