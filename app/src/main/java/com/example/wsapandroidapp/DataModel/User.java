package com.example.wsapandroidapp.DataModel;

public class User {

    private String id, authMethod, lastName, firstName, displayName;
    private UserRole role;

    public User() {
    }

    public User(String id, String authMethod) {
        this.id = id;
        this.authMethod = authMethod;
    }

    public String getId() {
        return id;
    }

    public String getAuthMethod() {
        return authMethod;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public UserRole getRole() {
        return role;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }
}
