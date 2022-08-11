package com.example.wsapandroidapp.DataModel;

public class UserWeddingTimelineTask {

    private String id, task;
    private int taskNo;
    private boolean completed;

    public UserWeddingTimelineTask() {
    }

    public UserWeddingTimelineTask(String id, String task, int taskNo, boolean completed) {
        this.id = id;
        this.task = task;
        this.taskNo = taskNo;
        this.completed = completed;
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

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
