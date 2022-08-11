package com.example.wsapandroidapp.DataModel;

import java.util.HashMap;
import java.util.Map;

public class ExpoExhibitors {

    private String id;
    private Map<String, ExpoExhibitor> expoExhibitors = new HashMap<>();

    public ExpoExhibitors() {
    }

    public ExpoExhibitors(String id, Map<String, ExpoExhibitor> expoExhibitors) {
        this.id = id;
        this.expoExhibitors = expoExhibitors;
    }

    public String getId() {
        return id;
    }

    public Map<String, ExpoExhibitor> getExpoExhibitors() {
        return expoExhibitors;
    }
}
