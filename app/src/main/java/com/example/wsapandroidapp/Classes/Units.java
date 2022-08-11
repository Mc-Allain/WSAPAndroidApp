package com.example.wsapandroidapp.Classes;

public class Units {

    private static final int MS_IN_SEC = 1000;
    private static final int SEC_IN_MIN = 60;
    private static final int SEC_IN_HOUR = SEC_IN_MIN * 60;
    private static final int HOUR_IN_DAY = 24;

    public static double msToSec(double milliseconds) {
        return milliseconds / MS_IN_SEC;
    }

    public static double msToMin(double milliseconds) {
        return milliseconds / MS_IN_SEC / SEC_IN_MIN;
    }

    public static double msToHour(double milliseconds) {
        return milliseconds / MS_IN_SEC / SEC_IN_HOUR;
    }

    public static double msToDay(double milliseconds) {
        return milliseconds / MS_IN_SEC / SEC_IN_HOUR / HOUR_IN_DAY;
    }

    public static double secToMs(double seconds) {
        return seconds * MS_IN_SEC;
    }

    public static double minToMs(double minutes) {
        return minutes * SEC_IN_MIN * MS_IN_SEC;
    }

    public static double hourToMs(double hours) {
        return hours * SEC_IN_HOUR * MS_IN_SEC;
    }

    public static double dayToMs(double days) {
        return days * HOUR_IN_DAY * SEC_IN_HOUR * MS_IN_SEC;
    }

}
