package com.example.wsapandroidapp.DataModel;

public class Expo {

    private String id, expo, description, image, division;
    private DateTimeRange dateTimeRange;

    public Expo() {
    }

    public Expo(String id, String expo, String description, String image, String division, DateTimeRange dateTimeRange) {
        this.id = id;
        this.expo = expo;
        this.description = description;
        this.image = image;
        this.division = division;
        this.dateTimeRange = dateTimeRange;
    }

    public String getId() {
        return id;
    }

    public String getExpo() {
        return expo;
    }

    public String getDescription() {
        return description;
    }

    public DateTimeRange getDateTimeRange() {
        return dateTimeRange;
    }

    public String getImage() {
        return image;
    }

    public String getDivision() {
        return division;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setExpo(String expo) {
        this.expo = expo;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setDivision(String division) {
        this.division = division;
    }

    public void setDateTimeRange(DateTimeRange dateTimeRange) {
        this.dateTimeRange = dateTimeRange;
    }
}
