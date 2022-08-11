package com.example.wsapandroidapp.DataModel;

public class Chapter {

    private String id, chapter, division;

    public Chapter() {
    }

    public Chapter(String id, String chapter, String division) {
        this.id = id;
        this.chapter = chapter;
        this.division = division;
    }

    public String getId() {
        return id;
    }

    public String getChapter() {
        return chapter;
    }

    public String getDivision() {
        return division;
    }
}
