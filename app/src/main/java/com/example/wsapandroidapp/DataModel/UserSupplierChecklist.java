package com.example.wsapandroidapp.DataModel;

public class UserSupplierChecklist {

    private String id, task;
    private int order;
    private String company, contactPerson, emailAddress;
    private long contactNumber;

    public UserSupplierChecklist() {
    }

    public UserSupplierChecklist(String id, String task, int order, String company, String contactPerson, String emailAddress, long contactNumber) {
        this.id = id;
        this.task = task;
        this.order = order;
        this.company = company;
        this.contactPerson = contactPerson;
        this.emailAddress = emailAddress;
        this.contactNumber = contactNumber;
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

    public String getCompany() {
        return company;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public long getContactNumber() {
        return contactNumber;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public void setContactNumber(long contactNumber) {
        this.contactNumber = contactNumber;
    }
}
