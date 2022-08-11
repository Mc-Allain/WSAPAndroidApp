package com.example.wsapandroidapp.Classes;

import android.text.format.DateFormat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateTime {

    private final SimpleDateFormat format = new SimpleDateFormat("yy/MM/dd HH:mm:ss", Locale.getDefault());

    private long dateTime = new Date().getTime();

    public DateTime() {
    }

    public DateTime(String dateString) {
        Date date = null;

        try {
            date = format.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        assert date != null;
        dateTime = date.getTime();
    }

    public DateTime(long dateTime) {
        this.dateTime = dateTime;
    }

    public void clearDateTime() {
        dateTime = new Date().getTime();
    }

    public void setDateTime(long dateTime) {
        this.dateTime = dateTime;
    }

    public void setDate(String dateString) {
        Date date = null;

        try {
            date = format.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        assert date != null;
        dateTime = date.getTime();
    }

    public String getDateTimeFormat() {
        return format.format(new Date(dateTime));
    }

    public String getDateText() {
        return DateFormat.format("MMMM dd, yyyy",new Date(dateTime)).toString();
    }

    public String getDate() {
        return DateFormat.format("yy/MM/dd",new Date(dateTime)).toString();
    }

    public String getYear() {
        return DateFormat.format("yyyy",new Date(dateTime)).toString();
    }

    public String getMonth() {
        return DateFormat.format("MM",new Date(dateTime)).toString();
    }

    public String getMonthText() {
        return DateFormat.format("MMMM",new Date(dateTime)).toString();
    }

    public String getMonthShortText() {
        return DateFormat.format("MMM",new Date(dateTime)).toString();
    }

    public String getDay() {
        return DateFormat.format("dd",new Date(dateTime)).toString();
    }

    public String getTime() {
        return DateFormat.format("HH:mm:ss",new Date(dateTime)).toString();
    }

    public String getHour() {
        return DateFormat.format("HH",new Date(dateTime)).toString();
    }

    public String getMin() {
        return DateFormat.format("mm",new Date(dateTime)).toString();
    }

    public String getSec() {
        return DateFormat.format("ss",new Date(dateTime)).toString();
    }

    public long getDateTimeValue() {
        return dateTime;
    }
}
