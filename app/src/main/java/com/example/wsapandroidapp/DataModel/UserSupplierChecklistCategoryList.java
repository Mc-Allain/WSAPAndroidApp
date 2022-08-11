package com.example.wsapandroidapp.DataModel;

import java.util.HashMap;
import java.util.Map;

public class UserSupplierChecklistCategoryList {

    private String id;
    private Map<String, UserSupplierChecklistCategory> supplierChecklistCategories = new HashMap<>();

    public UserSupplierChecklistCategoryList() {
    }

    public UserSupplierChecklistCategoryList(String id, Map<String, UserSupplierChecklistCategory> supplierChecklistCategories) {
        this.id = id;
        this.supplierChecklistCategories = supplierChecklistCategories;
    }

    public String getId() {
        return id;
    }

    public Map<String, UserSupplierChecklistCategory> getSupplierChecklistCategories() {
        return supplierChecklistCategories;
    }

    public void setSupplierChecklistCategories(Map<String, UserSupplierChecklistCategory> supplierChecklistCategories) {
        this.supplierChecklistCategories = supplierChecklistCategories;
    }
}
