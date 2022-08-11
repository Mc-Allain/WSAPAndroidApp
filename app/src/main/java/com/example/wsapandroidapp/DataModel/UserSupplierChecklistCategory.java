package com.example.wsapandroidapp.DataModel;

import java.util.HashMap;
import java.util.Map;

public class UserSupplierChecklistCategory {

    private String id, category;
    private int order;
    private Map<String, UserSupplierChecklist> checklist = new HashMap<>();

    public UserSupplierChecklistCategory() {
    }

    public UserSupplierChecklistCategory(String id, String category, int order, Map<String, UserSupplierChecklist> checklist) {
        this.id = id;
        this.category = category;
        this.order = order;
        this.checklist = checklist;
    }

    public String getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public int getOrder() {
        return order;
    }

    public Map<String, UserSupplierChecklist> getChecklist() {
        return checklist;
    }

    public void setChecklist(Map<String, UserSupplierChecklist> checklist) {
        this.checklist = checklist;
    }
}
