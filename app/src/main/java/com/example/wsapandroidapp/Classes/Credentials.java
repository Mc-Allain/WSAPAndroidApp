package com.example.wsapandroidapp.Classes;

import android.text.TextUtils;
import android.util.Patterns;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Credentials {

    public static int REQUIRED_PERSON_NAME_LENGTH = 2;
    public static int REQUIRED_LABEL_LENGTH = 2;
    public static int REQUIRED_PASSWORD_LENGTH = 6;
    public static int REQUIRED_PHONE_NUMBER_LENGTH_PH = 11;
    public static int REQUIRED_PHONE_NUMBER_LENGTH_PH_WITH_PREFIX = REQUIRED_PHONE_NUMBER_LENGTH_PH - 1;

    public static String ALPHA_NUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";

    public static String fullTrim(String string) {
        if (string == null) return null;

        String[] splitStrings = string.split(" ");
        List<String> trimmedStrings = new ArrayList<>();

        for (String splitString : splitStrings)
            if (!splitString.equals(" ") && splitString.trim().length() != 0)
                trimmedStrings.add(splitString.trim());

        return TextUtils.join(" ", trimmedStrings);
    }

    public static boolean isEmpty(String string) {
        return string == null || fullTrim(string).length() == 0;
    }

    public static boolean isValidLength(String string, int minLength, int maxLength) {
        return !isEmpty(string) && fullTrim(string).length() >= minLength &&
                (maxLength == 0 || fullTrim(string).length() <= maxLength);
    }

    public static boolean isValidPhoneNumber(String phoneNumber, int length) {
        return !isEmpty(phoneNumber) && Patterns.PHONE.matcher(phoneNumber).matches()
                && phoneNumber.length() == length;
    }

    public static boolean isValidPhoneNumber(String phoneNumber, int minLength, int maxLength) {
        return !isEmpty(phoneNumber) && Patterns.PHONE.matcher(phoneNumber).matches()
                && phoneNumber.length() >= minLength && phoneNumber.length() <= maxLength;
    }

    public static boolean isValidEmailAddress(String emailAddress) {
        return !isEmpty(emailAddress) && Patterns.EMAIL_ADDRESS.matcher(emailAddress).matches();
    }

    public static boolean isValidPassword(String password) {
        return !isEmpty(password) && password.matches("[A-Za-z0-9]*");
    }

    public static boolean isPasswordMatch(String password1, String password2) {
        return !isEmpty(password2) && password1.equals(password2);
    }

    public static boolean isValidLink(String link) {
        return !isEmpty(link) && Patterns.WEB_URL.matcher(link).matches();
    }

    public static String getUniqueId() {
        StringBuilder idBuilder = new StringBuilder();

        for (int i = 0; i < 28; i++) {
            Random rnd = new Random();
            idBuilder.append(ALPHA_NUMERIC.charAt(rnd.nextInt(ALPHA_NUMERIC.length())));
        }

        return idBuilder.toString();
    }

    public static String getSentenceCase(String string) {

        if (string == null) return "";

        int pos = 0;
        boolean capitalize = true;
        StringBuilder stringBuilder = new StringBuilder(string);

        while (pos < stringBuilder.length()) {

            if (capitalize && !Character.isWhitespace(stringBuilder.charAt(pos)))
                stringBuilder.setCharAt(pos, Character.toUpperCase(stringBuilder.charAt(pos)));
            else if (!capitalize && !Character.isWhitespace(stringBuilder.charAt(pos)))
                stringBuilder.setCharAt(pos, Character.toLowerCase(stringBuilder.charAt(pos)));

            capitalize = stringBuilder.charAt(pos) == '.' || (capitalize && Character.isWhitespace(stringBuilder.charAt(pos)));

            pos++;
        }

        return stringBuilder.toString();
    }
}
