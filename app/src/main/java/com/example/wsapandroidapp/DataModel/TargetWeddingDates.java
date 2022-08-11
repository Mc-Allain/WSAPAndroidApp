package com.example.wsapandroidapp.DataModel;

import java.util.HashMap;
import java.util.Map;

public class TargetWeddingDates {

    private String id;
    private Map<String, TargetWeddingDate> targetWeddingDates = new HashMap<>();

    public TargetWeddingDates() {
    }

    public TargetWeddingDates(String id, Map<String, TargetWeddingDate> targetWeddingDates) {
        this.id = id;
        this.targetWeddingDates = targetWeddingDates;
    }

    public String getId() {
        return id;
    }

    public Map<String, TargetWeddingDate> getTargetWeddingDates() {
        return targetWeddingDates;
    }
}
