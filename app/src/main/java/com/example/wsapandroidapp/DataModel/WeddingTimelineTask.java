package com.example.wsapandroidapp.DataModel;

public class WeddingTimelineTask {

    private String id, task;
    private int taskNo;

    public WeddingTimelineTask() {
    }

    public WeddingTimelineTask(String id, String task, int taskNo) {
        this.id = id;
        this.task = task;
        this.taskNo = taskNo;
    }

    public String getId() {
        return id;
    }

    public String getTask() {
        return task;
    }

    public int getTaskNo() {
        return taskNo;
    }
}
