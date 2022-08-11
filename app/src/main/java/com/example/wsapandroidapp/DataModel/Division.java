package com.example.wsapandroidapp.DataModel;

public class Division {

    private String id, division, image;

    public Division() {
    }

    public Division(String id, String division, String image) {
        this.id = id;
        this.division = division;
        this.image = image;
    }

    public String getId() {
        return id;
    }

    public String getDivision() {
        return division;
    }

    public String getImage() {
        return image;
    }
}
