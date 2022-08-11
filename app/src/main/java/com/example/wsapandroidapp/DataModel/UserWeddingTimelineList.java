package com.example.wsapandroidapp.DataModel;

import java.util.HashMap;
import java.util.Map;

public class UserWeddingTimelineList {

    private String id;
    private long targetWeddingDate;
    private Map<String, UserWeddingTimeline> weddingTimeline = new HashMap<>();

    public UserWeddingTimelineList() {
    }

    public UserWeddingTimelineList(String id, long targetWeddingDate, Map<String, UserWeddingTimeline> weddingTimeline) {
        this.id = id;
        this.targetWeddingDate = targetWeddingDate;
        this.weddingTimeline = weddingTimeline;
    }

    public String getId() {
        return id;
    }

    public long getTargetWeddingDate() {
        return targetWeddingDate;
    }

    public Map<String, UserWeddingTimeline> getWeddingTimeline() {
        return weddingTimeline;
    }

    public void setTargetWeddingDate(long targetWeddingDate) {
        this.targetWeddingDate = targetWeddingDate;
    }

    public void setWeddingTimeline(Map<String, UserWeddingTimeline> weddingTimeline) {
        this.weddingTimeline = weddingTimeline;
    }
}
