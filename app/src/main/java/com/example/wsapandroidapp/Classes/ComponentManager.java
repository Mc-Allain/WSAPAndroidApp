package com.example.wsapandroidapp.Classes;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.speech.RecognizerIntent;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.wsapandroidapp.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import androidx.core.content.res.ResourcesCompat;

public class ComponentManager {

    private final int DRAWABLE_LEFT = 0;
    private final int DRAWABLE_TOP = 1;
    private final int DRAWABLE_RIGHT = 2;
    private final int DRAWABLE_BOTTOM = 3;

    Context context;

    private List<TextView> errorTextViewList = new ArrayList<>();
    private List<EditText> errorEditTextList = new ArrayList<>();

    Calendar calendar = Calendar.getInstance();

    public ComponentManager(Context context) {
        this.context = context;
    }

    public void initializeErrorComponents(List<TextView> errorTextViewList, List<EditText> errorEditTextList) {
        this.errorTextViewList = errorTextViewList;
        this.errorEditTextList = errorEditTextList;
    }

    public void hideInputErrors() {
        for (TextView errorTextView : errorTextViewList)
            errorTextView.setVisibility(View.GONE);
        for (EditText errorEditText : errorEditTextList)
            errorEditText.setBackground(
                    ResourcesCompat.getDrawable(context.getResources(), R.drawable.primary_input_bg, null)
            );
    }

    public void hideInputError(TextView textView, EditText targetEditText) {
        textView.setVisibility(View.GONE);
        targetEditText.setBackground(
                ResourcesCompat.getDrawable(context.getResources(), R.drawable.primary_input_bg, null)
        );
    }

    public void hideInputError(TextView textView) {
        textView.setVisibility(View.GONE);
    }

    public void showInputError(TextView textView, String string, EditText targetEditText) {
        textView.setVisibility(View.VISIBLE);
        textView.setText(string);
        targetEditText.setBackground(
                ResourcesCompat.getDrawable(context.getResources(), R.drawable.error_input_bg, null)
        );
    }

    public void showInputError(TextView textView, String string) {
        textView.setVisibility(View.VISIBLE);
        textView.setText(string);
    }

    public boolean isNoInputError() {
        for (TextView errorTextView : errorTextViewList)
            if (errorTextView.getVisibility() == View.VISIBLE)
                return false;
        return true;
    }

    public void setInputLeftDrawable(EditText targetEditText, boolean show, int drawable) {
        Drawable leftDrawable = null;
        if (show) leftDrawable = ResourcesCompat.getDrawable(context.getResources(), drawable, null);
        targetEditText.setCompoundDrawablesWithIntrinsicBounds(
                leftDrawable,
                targetEditText.getCompoundDrawables()[DRAWABLE_TOP],
                targetEditText.getCompoundDrawables()[DRAWABLE_RIGHT],
                targetEditText.getCompoundDrawables()[DRAWABLE_BOTTOM]);
    }

    public void setInputRightDrawable(EditText targetEditText, boolean show, int drawable) {
        Drawable rightDrawable = null;
        if (show) rightDrawable = ResourcesCompat.getDrawable(context.getResources(), drawable, null);
        targetEditText.setCompoundDrawablesWithIntrinsicBounds(
                targetEditText.getCompoundDrawables()[DRAWABLE_LEFT],
                targetEditText.getCompoundDrawables()[DRAWABLE_TOP],
                rightDrawable,
                targetEditText.getCompoundDrawables()[DRAWABLE_BOTTOM]);


        if (drawable == Enums.VOICE_RECOGNITION)
            targetEditText.setOnTouchListener((view, motionEvent) -> {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if (targetEditText.getCompoundDrawables()[DRAWABLE_RIGHT] != null &&
                            motionEvent.getRawX() >= targetEditText.getRight() - 32 -
                                    targetEditText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width()) {
                        onDrawableClick(targetEditText, drawable);
                        return true;
                    }
                }

                return false;
            });
        else targetEditText.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                if (targetEditText.getCompoundDrawables()[DRAWABLE_RIGHT] != null &&
                        motionEvent.getRawX() >= targetEditText.getRight() -
                                targetEditText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width()) {
                    onDrawableClick(targetEditText, drawable);
                    return true;
                }
            }

            return false;
        });
    }

    private void onDrawableClick(EditText targetEditText, int drawable) {
        boolean isFocused = targetEditText.isFocused();

        if (drawable == Enums.CLEAR_TEXT)
            targetEditText.getText().clear();

        if (drawable == Enums.SHOW_PASSWORD) {
            targetEditText.setTransformationMethod(null);
            if (passwordListener != null) passwordListener.onPasswordToggle(true);
        }

        if (drawable == Enums.HIDE_PASSWORD) {
            targetEditText.setTransformationMethod(new PasswordTransformationMethod());
            if (passwordListener != null) passwordListener.onPasswordToggle(false);
        }

        if (drawable == Enums.VOICE_RECOGNITION)
            if (voiceRecognitionListener != null) voiceRecognitionListener.onClick();

        if (drawable == Enums.CALENDAR_ICON)
            showDatePickerDialog(targetEditText);

        if (!isFocused) targetEditText.clearFocus();
    }

    public Intent voiceRecognitionIntent() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Please start speaking.");
        return intent;
    }

    private VoiceRecognitionListener voiceRecognitionListener;

    public interface VoiceRecognitionListener {
        void onClick();
    }

    public void setVoiceRecognitionListener(VoiceRecognitionListener voiceRecognitionListener) {
        this.voiceRecognitionListener = voiceRecognitionListener;
    }

    public void showDatePickerDialog(EditText targetEditText) {
        int calendarYear = calendar.get(Calendar.YEAR);
        int calendarMonth = calendar.get(Calendar.MONTH);
        int calendarDay = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(context,
                (datePicker, i, i1, i2) -> {
                    int year = datePicker.getYear();
                    int month = datePicker.getMonth() + 1;
                    int day = datePicker.getDayOfMonth();
                    String stringDateTime = year + "/" + month + "/" + day + " 00:00:00";
                    DateTime dateTime = new DateTime(stringDateTime);

                    if (datePickerListener != null) {
                        datePickerListener.onSelect(dateTime.getDateTimeValue(), targetEditText);
                        datePickerListener.onSelect(dateTime.getDateText(), targetEditText);
                    }

                }, calendarYear, calendarMonth, calendarDay );

        datePickerDialog.show();
    }

    public void showDatePickerDialog(TextView targetTextView) {
        int calendarYear = calendar.get(Calendar.YEAR);
        int calendarMonth = calendar.get(Calendar.MONTH);
        int calendarDay = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(context,
                (datePicker, i, i1, i2) -> {
                    int year = datePicker.getYear();
                    int month = datePicker.getMonth() + 1;
                    int day = datePicker.getDayOfMonth();
                    String stringDateTime = year + "/" + month + "/" + day + " 00:00:00";
                    DateTime dateTime = new DateTime(stringDateTime);

                    if (datePickerListener != null) {
                        datePickerListener.onSelect(dateTime.getDateTimeValue(), targetTextView);
                        datePickerListener.onSelect(dateTime.getDateText(), targetTextView);
                    }

                }, calendarYear, calendarMonth, calendarDay );

        datePickerDialog.show();
    }

    private DatePickerListener datePickerListener;

    public interface DatePickerListener {
        void onSelect(long dateTime, EditText targetEditText);
        void onSelect(String date, EditText targetEditText);
        void onSelect(long dateTime, TextView targetTextView);
        void onSelect(String date, TextView targetTextView);
    }

    public void setDatePickerListener(DatePickerListener datePickerListener) {
        this.datePickerListener = datePickerListener;
    }

    public void setPrimaryButtonEnabled(Button button, boolean isEnabled) {
        button.setEnabled(isEnabled);

        if (isEnabled) {
            button.setBackgroundColor(context.getColor(R.color.primary));
            button.setTextColor(context.getColor(R.color.white));
        } else {
            button.setBackgroundColor(context.getColor(R.color.gray));
            button.setTextColor(context.getColor(R.color.darker_gray));
        }
    }

    private PasswordListener passwordListener;

    public interface PasswordListener {
        void onPasswordToggle(boolean isPasswordShownResult);
    }

    public void setPasswordListener(PasswordListener passwordListener) {
        this.passwordListener = passwordListener;
    }
}
