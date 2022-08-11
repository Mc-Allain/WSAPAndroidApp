package com.example.wsapandroidapp.DataModel;

import java.util.HashMap;
import java.util.Map;

public class WeddingTimeline {

    private String id, title;
    private long duration;
    private int timelineOrder;
    private Map<String, WeddingTimelineTask> tasks = new HashMap<>();

    public WeddingTimeline() {
    }

    public WeddingTimeline(String id, String title, long duration, int timelineOrder, Map<String, WeddingTimelineTask> tasks) {
        this.id = id;
        this.title = title;
        this.duration = duration;
        this.timelineOrder = timelineOrder;
        this.tasks = tasks;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public long getDuration() {
        return duration;
    }

    public int getTimelineOrder() {
        return timelineOrder;
    }

    public Map<String, WeddingTimelineTask> getTasks() {
        return tasks;
    }
}
