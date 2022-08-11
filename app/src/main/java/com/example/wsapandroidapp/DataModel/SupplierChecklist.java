package com.example.wsapandroidapp.DataModel;

public class SupplierChecklist {

    private String id, task;
    private int order;

    public SupplierChecklist() {
    }

    public SupplierChecklist(String id, String task, int order) {
        this.id = id;
        this.task = task;
        this.order = order;
    }

    public String getId() {
        return id;
    }

    public String getTask() {
        return task;
    }

    public int getOrder() {
        return order;
    }
}
