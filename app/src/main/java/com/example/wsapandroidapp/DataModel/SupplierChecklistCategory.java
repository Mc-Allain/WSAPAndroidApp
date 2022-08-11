package com.example.wsapandroidapp.DataModel;

import java.util.HashMap;
import java.util.Map;

public class SupplierChecklistCategory {

    private String id, category;
    private int order;
    private Map<String, SupplierChecklist> checklist = new HashMap<>();

    public SupplierChecklistCategory() {
    }

    public SupplierChecklistCategory(String id, String category, int order, Map<String, SupplierChecklist> checklist) {
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

    public Map<String, SupplierChecklist> getChecklist() {
        return checklist;
    }

    public void setChecklist(Map<String, SupplierChecklist> checklist) {
        this.checklist = checklist;
    }
}
