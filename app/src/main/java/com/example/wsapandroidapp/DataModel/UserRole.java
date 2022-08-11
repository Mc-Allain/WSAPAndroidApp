package com.example.wsapandroidapp.DataModel;

public class UserRole {

    private boolean admin, adminHead, developer, leadDeveloper;

    public UserRole() {
    }

    public UserRole(boolean admin, boolean adminHead, boolean developer, boolean leadDeveloper) {
        this.admin = admin;
        this.adminHead = adminHead;
        this.developer = developer;
        this.leadDeveloper = leadDeveloper;
    }

    public boolean isAdmin() {
        return admin;
    }

    public boolean isAdminHead() {
        return adminHead;
    }

    public boolean isDeveloper() {
        return developer;
    }

    public boolean isLeadDeveloper() {
        return leadDeveloper;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public void setAdminHead(boolean adminHead) {
        this.adminHead = adminHead;
    }

    public void setDeveloper(boolean developer) {
        this.developer = developer;
    }

    public void setLeadDeveloper(boolean leadDeveloper) {
        this.leadDeveloper = leadDeveloper;
    }
}
