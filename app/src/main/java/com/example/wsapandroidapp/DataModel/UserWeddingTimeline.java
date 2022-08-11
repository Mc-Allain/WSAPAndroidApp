package com.example.wsapandroidapp.DataModel;

import java.util.HashMap;
import java.util.Map;

public class UserWeddingTimeline {

    private String id, title;
    private long duration;
    private int timelineOrder;
    private Map<String, UserWeddingTimelineTask> tasks = new HashMap<>();

    public UserWeddingTimeline() {
    }

    public UserWeddingTimeline(String id, String title, long duration, int timelineOrder, Map<String, UserWeddingTimelineTask> tasks) {
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

    public Map<String, UserWeddingTimelineTask> getTasks() {
        return tasks;
    }

    public void setTasks(Map<String, UserWeddingTimelineTask> tasks) {
        this.tasks = tasks;
    }
}
