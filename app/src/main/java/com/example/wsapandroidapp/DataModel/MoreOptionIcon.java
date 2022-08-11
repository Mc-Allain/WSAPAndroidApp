package com.example.wsapandroidapp.DataModel;

import android.content.Intent;

public class MoreOptionIcon {

    private final String option;
    private final int icon;
    private final Intent intent;

    public MoreOptionIcon(String option, int icon, Intent intent) {
        this.option = option;
        this.icon = icon;
        this.intent = intent;
    }

    public String getOption() {
        return option;
    }

    public int getIcon() {
        return icon;
    }

    public Intent getIntent() {
        return intent;
    }
}
