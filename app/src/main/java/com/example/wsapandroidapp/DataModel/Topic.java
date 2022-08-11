package com.example.wsapandroidapp.DataModel;

public class Topic {

    private String id, topic, description, category;

    public Topic() {
    }

    public Topic(String id, String topic, String description, String category) {
        this.id = id;
        this.topic = topic;
        this.description = description;
        this.category = category;
    }

    public String getId() {
        return id;
    }

    public String getTopic() {
        return topic;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }
}
