package com.example.wsapandroidapp.DataModel;

public class ExpoExhibitor {

    private String id;
    private boolean member;

    public ExpoExhibitor() {
    }

    public ExpoExhibitor(String id, boolean member) {
        this.id = id;
        this.member = member;
    }

    public String getId() {
        return id;
    }

    public boolean isMember() {
        return member;
    }
}
