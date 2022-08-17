package com.example.wsapandroidapp.DataModel;

import java.util.HashMap;
import java.util.Map;

public class UserSupplierComparativeSheetCategoryList {

    private String id;
    private Map<String, UserSupplierComparativeSheetCategory> supplierComparativeSheetCategories = new HashMap<>();

    public UserSupplierComparativeSheetCategoryList() {
    }

    public UserSupplierComparativeSheetCategoryList(String id, Map<String, UserSupplierComparativeSheetCategory> supplierComparativeSheetCategories) {
        this.id = id;
        this.supplierComparativeSheetCategories = supplierComparativeSheetCategories;
    }

    public String getId() {
        return id;
    }

    public Map<String, UserSupplierComparativeSheetCategory> getSupplierComparativeSheetCategories() {
        return supplierComparativeSheetCategories;
    }

    public void setSupplierComparativeSheetCategories(Map<String, UserSupplierComparativeSheetCategory> supplierComparativeSheetCategories) {
        this.supplierComparativeSheetCategories = supplierComparativeSheetCategories;
    }
}
