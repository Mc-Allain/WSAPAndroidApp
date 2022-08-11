package com.example.wsapandroidapp.DataModel;

public class CategoryImage {

    private String id, category, image;

    public CategoryImage() {
    }

    public CategoryImage(String id, String category, String image) {
        this.id = id;
        this.category = category;
        this.image = image;
    }

    public String getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public String getImage() {
        return image;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
