package com.example.viktorjankov.shuttletracker.model;

public class Rider {

    // Gotten from main app
    private double mLatitude;
    private double mLongitude;
    private String mDestinationName;
    private boolean active;
    private double mDestinationTime;
    private double mProximity;

    // User personal info
    private String firstName;
    private String uID;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    private String companyID;

    public Rider(String firstName, String uID, String companyID) {
        this.uID = uID;
        this.companyID = companyID;
        this.firstName = firstName;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public void setLatitude(double latitude) {
        this.mLatitude = latitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public void setLongitude(double longitude) {
        this.mLongitude = longitude;
    }

    public String getDestinationName() {
        return mDestinationName;
    }

    public void setDestinationName(String destinationName) {
        this.mDestinationName = destinationName;
    }

    public double getDestinationTime() {
        return mDestinationTime;
    }

    public void setDestinationTime(Double destinationTime) {
        this.mDestinationTime = destinationTime;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getuID() {
        return uID;
    }

    public void setuID(String uID) {
        this.uID = uID;
    }

    public String getCompanyID() {
        return companyID;
    }

    public void setCompanyID(String companyID) {

        this.companyID = companyID;
    }

    public double getProximity() {
        return mProximity;
    }

    public void setProximity(double mProximity) {
        this.mProximity = mProximity;
    }
}
