package com.example.wsapandroidapp.Classes;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.wsapandroidapp.R;

import androidx.appcompat.app.AppCompatDelegate;

public class Theme {

    private final Context context;

    public Theme(Context context) {
        this.context = context;

        applyTheme();
    }

    private void applyTheme() {
        if (getTheme().equals(Enums.THEME_LIGHT)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else if (getTheme().equals(Enums.THEME_DARK)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else if (getTheme().equals(Enums.THEME_SYSTEM)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
    }

    public String getTheme() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("theme", Context.MODE_PRIVATE);
        return sharedPreferences.getString("selectedTheme", Enums.THEME_LIGHT);
    }

    public void setTheme(String selectedTheme) {
        SharedPreferences sharedPreferences =
                context.getSharedPreferences("theme", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("selectedTheme", selectedTheme);
        editor.apply();

        applyTheme();
    }
}
