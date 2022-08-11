package com.example.wsapandroidapp.DataModel;

public class ContactInfo {

    private String emailAddress, locationAddress;
    private long phoneNumber;

    public ContactInfo() {
    }

    public ContactInfo(String emailAddress, String locationAddress, long phoneNumber) {
        this.emailAddress = emailAddress;
        this.locationAddress = locationAddress;
        this.phoneNumber = phoneNumber;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getLocationAddress() {
        return locationAddress;
    }

    public long getPhoneNumber() {
        return phoneNumber;
    }
}
