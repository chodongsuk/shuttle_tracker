package com.example.viktorjankov.shuttletracker.model;

public class User {

    private String userName;
    private double latitude;
    private double longitude;
    private String destinationName;
    private String destinationTime;
    private boolean active;

    public User() {

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

    public User(String userName) {
        this.userName = userName;
    }

    public User(String userName, String destinationLocation) {
        this.destinationName = destinationLocation;
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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
                "name: " + userName + "\n" +
                "destination: " + destinationName + "\n" +
                "destination_time: " + destinationTime + "\n" +
                "active: " + active;

    }
}

