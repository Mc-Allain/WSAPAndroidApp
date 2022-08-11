package com.example.wsapandroidapp.DataModel;

import android.content.Intent;

public class MenuCategoryImage {

    private String category;
    private int image;
    private Intent intent;

    public MenuCategoryImage() {
    }

    public MenuCategoryImage(String category, int image, Intent intent) {
        this.category = category;
        this.image = image;
        this.intent = intent;
    }

    public String getCategory() {
        return category;
    }

    public int getImage() {
        return image;
    }

    public Intent getIntent() {
        return intent;
    }
}
