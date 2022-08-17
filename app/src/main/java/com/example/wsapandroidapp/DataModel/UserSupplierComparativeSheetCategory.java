package com.example.wsapandroidapp.DataModel;

import java.util.HashMap;
import java.util.Map;

public class UserSupplierComparativeSheetCategory {

    private String id, category;
    private int order;
    private Map<String, UserSupplierComparativeSheetItem> items = new HashMap<>();

    public UserSupplierComparativeSheetCategory() {
    }

    public UserSupplierComparativeSheetCategory(String id, String category, int order, Map<String, UserSupplierComparativeSheetItem> items) {
        this.id = id;
        this.category = category;
        this.order = order;
        this.items = items;
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

    public Map<String, UserSupplierComparativeSheetItem> getItems() {
        return items;
    }

    public void setItems(Map<String, UserSupplierComparativeSheetItem> items) {
        this.items = items;
    }
}
