package com.example.wsapandroidapp.DataModel;

import java.util.HashMap;
import java.util.Map;

public class UserSupplierComparativeSheetItem {

    private String id, item, remarks;
    private int order;
    private Map<String, SupplierOption> options = new HashMap<>();

    public UserSupplierComparativeSheetItem() {
    }

    public UserSupplierComparativeSheetItem(String id, String item, String remarks, int order, Map<String, SupplierOption> options) {
        this.id = id;
        this.item = item;
        this.remarks = remarks;
        this.order = order;
        this.options = options;
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

    public String getRemarks() {
        return remarks;
    }

    public Map<String, SupplierOption> getOptions() {
        return options;
    }

    public void setOptions(Map<String, SupplierOption> options) {
        this.options = options;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
