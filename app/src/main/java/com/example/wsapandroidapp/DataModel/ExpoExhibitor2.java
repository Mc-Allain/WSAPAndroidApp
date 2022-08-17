package com.example.wsapandroidapp.DataModel;

public class ExpoExhibitor2 {

    private String id;
    private boolean member;

    public ExpoExhibitor2() {
    }

    public ExpoExhibitor2(String id, boolean member) {
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
