package com.example.wsapandroidapp.DataModel;

public class TargetWeddingDate {

    private String id;
    private long targetWeddingDate;

    public TargetWeddingDate() {
    }

    public TargetWeddingDate(String id, long targetWeddingDate) {
        this.id = id;
        this.targetWeddingDate = targetWeddingDate;
    }

    public String getId() {
        return id;
    }

    public long getTargetWeddingDate() {
        return targetWeddingDate;
    }
}
