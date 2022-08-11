package com.example.wsapandroidapp.Classes;

public class Compare {
    public static boolean containsIgnoreCase(String string, String anotherString) {
        return string.toLowerCase().contains(anotherString.toLowerCase());
    }
}
