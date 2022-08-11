package com.example.wsapandroidapp.DataModel;

public class SocialMedia {

    String facebook, instagram, twitter, youtube, website;

    public SocialMedia() {
    }

    public SocialMedia(String facebook, String instagram, String twitter, String youtube, String website) {
        this.facebook = facebook;
        this.instagram = instagram;
        this.twitter = twitter;
        this.youtube = youtube;
        this.website = website;
    }

    public String getFacebook() {
        return facebook;
    }

    public String getInstagram() {
        return instagram;
    }

    public String getTwitter() {
        return twitter;
    }

    public String getYoutube() {
        return youtube;
    }

    public String getWebsite() {
        return website;
    }
}
