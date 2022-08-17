package com.example.wsapandroidapp.DataModel;

public class SupplierComparativeSheetItem {

    private String id, item;
    private int order;

    public SupplierComparativeSheetItem() {
    }

    public SupplierComparativeSheetItem(String id, String item, int order) {
        this.id = id;
        this.item = item;
        this.order = order;
    }

    public String getId() {
        return id;
    }

    public String getItem() {
        return item;
    }

    public int getOrder() {
        return order;
    }
}
