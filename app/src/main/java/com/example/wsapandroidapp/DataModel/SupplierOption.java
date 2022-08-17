package com.example.wsapandroidapp.DataModel;

public class SupplierOption {

    private String id, option;
    private int order;

    public SupplierOption() {
    }

    public SupplierOption(String id, String option, int order) {
        this.id = id;
        this.option = option;
        this.order = order;
    }

    public String getId() {
        return id;
    }

    public String getOption() {
        return option;
    }

    public int getOrder() {
        return order;
    }

    public void setOption(String option) {
        this.option = option;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
