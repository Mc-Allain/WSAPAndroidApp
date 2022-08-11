package com.example.wsapandroidapp.DataModel;

public class Exhibitor {

    private String id, exhibitor, description, image;
    private String category;
    private ContactInfo contactInfo;
    private SocialMedia socialMedia;

    public Exhibitor() {
    }

    public Exhibitor(String id, String exhibitor, String description, String image, String category) {
        this.id = id;
        this.exhibitor = exhibitor;
        this.description = description;
        this.image = image;
        this.category = category;
    }

    public Exhibitor(String id, String exhibitor, String description, String image, String category, ContactInfo contactInfo, SocialMedia socialMedia) {
        this.id = id;
        this.exhibitor = exhibitor;
        this.description = description;
        this.image = image;
        this.category = category;
        this.contactInfo = contactInfo;
        this.socialMedia = socialMedia;
    }

    public String getId() {
        return id;
    }

    public String getExhibitor() {
        return exhibitor;
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

    public ContactInfo getContactInfo() {
        return contactInfo;
    }

    public SocialMedia getSocialMedia() {
        return socialMedia;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setExhibitor(String exhibitor) {
        this.exhibitor = exhibitor;
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

    public void setContactInfo(ContactInfo contactInfo) {
        this.contactInfo = contactInfo;
    }

    public void setSocialMedia(SocialMedia socialMedia) {
        this.socialMedia = socialMedia;
    }
}
