package com.example.wsapandroidapp.DataModel;

public class ExpoExhibitor2 {

    private String id, exhibitor, image;
    private boolean member;

    public ExpoExhibitor2() {
    }

    public ExpoExhibitor2(String id, String exhibitor, String image, boolean member) {
        this.id = id;
        this.exhibitor = exhibitor;
        this.image = image;
        this.member = member;
    }

    public String getId() {
        return id;
    }

    public String getExhibitor() {
        return exhibitor;
    }

    public String getImage() {
        return image;
    }

    public boolean isMember() {
        return member;
    }
}
