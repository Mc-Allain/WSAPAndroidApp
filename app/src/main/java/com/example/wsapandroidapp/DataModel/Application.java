package com.example.wsapandroidapp.DataModel;

public class Application {

    private double latestVersion;
    private String status, downloadLink;

    public Application() {
    }

    public Application(double latestVersion, String status) {
        this.latestVersion = latestVersion;
        this.status = status;
    }

    public double getLatestVersion() {
        return latestVersion;
    }

    public double getCurrentVersion() {
        return 0.01;
    }

    public String getStatus() {
        return status;
    }

    public String getDownloadLink() {
        return downloadLink;
    }

    public boolean isForDeveloper() {
        return false;
    }
}
