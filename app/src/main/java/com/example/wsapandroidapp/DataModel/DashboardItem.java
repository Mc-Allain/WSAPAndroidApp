package com.example.wsapandroidapp.DataModel;

import android.content.Intent;

public class DashboardItem {

    private String dashboard;
    private int image;
    private Intent intent;

    public DashboardItem() {
    }

    public DashboardItem(String dashboard, int image, Intent intent) {
        this.dashboard = dashboard;
        this.image = image;
        this.intent = intent;
    }

    public String getDashboard() {
        return dashboard;
    }

    public int getImage() {
        return image;
    }

    public Intent getIntent() {
        return intent;
    }
}
