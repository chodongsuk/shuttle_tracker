package com.example.viktorjankov.shuttletracker.model;

import java.io.Serializable;

public class User implements Serializable {

    // Gotten from registration page
    private String firstName;
    private String lastName;
    private String email;
    private String companyCode;

    // Gotten from main app
    private double latitude;
    private double longitude;
    private String destinationName;
    private String destinationTime;
    private boolean active;

    public User() { }

    public User(String firstName) {
        this.firstName = firstName;
    }

    public User(String firstName, String destinationLocation) {
        this.destinationName = destinationLocation;
        this.firstName = firstName;
    }

    public User(String firstName, String lastName, String email, String companyCode) {

        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.companyCode = companyCode;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCompanyCode() {
        return companyCode;
    }

    public void setCompanyCode(String companyCode) {
        this.companyCode = companyCode;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getDestinationName() {
        return destinationName;
    }

    public void setDestinationName(String destinationName) {
        this.destinationName = destinationName;
    }

    public String getDestinationTime() {
        return destinationTime;
    }

    public void setDestinationTime(String destinationTime) {
        this.destinationTime = destinationTime;
    }

    @Override
    public String toString() {
        return "\n" +
                "company code: " + companyCode + "\n" +
                "email: " + email + "\n" +
                "first: " + firstName + "\n" +
                "last: " + lastName + "\n" +
                "destination: " + destinationName + "\n" +
                "destination_time: " + destinationTime + "\n" +
                "active: " + active;
    }
}

