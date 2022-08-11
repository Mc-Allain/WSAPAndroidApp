package com.example.wsapandroidapp.DataModel;

public class Supplier {

    private String id, supplier, description, image;
    private String category, chapter;
    private ContactInfo contactInfo;
    private SocialMedia socialMedia;

    public Supplier() {
    }

    public Supplier(String id, String supplier, String description, String image, String category, String chapter) {
        this.id = id;
        this.supplier = supplier;
        this.description = description;
        this.image = image;
        this.category = category;
        this.chapter = chapter;
    }

    public Supplier(String id, String supplier, String description, String image, String category, String chapter, ContactInfo contactInfo, SocialMedia socialMedia) {
        this.id = id;
        this.supplier = supplier;
        this.description = description;
        this.image = image;
        this.category = category;
        this.chapter = chapter;
        this.contactInfo = contactInfo;
        this.socialMedia = socialMedia;
    }

    public String getId() {
        return id;
    }

    public String getSupplier() {
        return supplier;
    }

    public String getDescription() {
        return description;
    }

    public String getImage() {
        return image;
    }

    public String getCategory() {
        return category;
    }

    public String getChapter() {
        return chapter;
    }

    public ContactInfo getContactInfo() {
        return contactInfo;
    }

    public SocialMedia getSocialMedia() {
        return socialMedia;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setChapter(String chapter) {
        this.chapter = chapter;
    }

    public void setContactInfo(ContactInfo contactInfo) {
        this.contactInfo = contactInfo;
    }

    public void setSocialMedia(SocialMedia socialMedia) {
        this.socialMedia = socialMedia;
    }
}
