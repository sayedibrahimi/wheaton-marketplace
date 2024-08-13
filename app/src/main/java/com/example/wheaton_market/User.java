package com.example.wheaton_market;

public class User {
    private String firstName;
    private String lastName;
    private String wheatonID;
    private String email;
    private String userPictureURL;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String firstName, String lastName, String wheatonID, String email, String userPictureURL) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.wheatonID = wheatonID;
        this.email = email;
        this.userPictureURL = userPictureURL;
    }

    // Getters
    public String getFirstName() {
        return firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public String getWheatonID() {
        return wheatonID;
    }
    public String getEmail() {
        return email;
    }
    public String getUserPictureURL() {
        return userPictureURL;
    }

    // Setters
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setWheatonID(String wheatonID) {
        this.wheatonID = wheatonID;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    public void setUserPictureURL(String userPictureURL) {
        this.userPictureURL = userPictureURL;
    }
}
