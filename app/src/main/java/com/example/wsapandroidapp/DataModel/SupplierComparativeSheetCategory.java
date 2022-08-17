package com.example.wsapandroidapp.DataModel;

import java.util.HashMap;
import java.util.Map;

public class SupplierComparativeSheetCategory {

    private String id, category;
    private int order;
    private Map<String, SupplierComparativeSheetItem> items = new HashMap<>();

    public SupplierComparativeSheetCategory() {
    }

    public SupplierComparativeSheetCategory(String id, String category, int order, Map<String, SupplierComparativeSheetItem> items) {
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

    public Map<String, SupplierComparativeSheetItem> getItems() {
        return items;
    }

    public void setItems(Map<String, SupplierComparativeSheetItem> items) {
        this.items = items;
    }
}
