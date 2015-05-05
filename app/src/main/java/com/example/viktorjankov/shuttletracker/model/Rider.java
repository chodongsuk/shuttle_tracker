package com.example.viktorjankov.shuttletracker.model;

import java.io.Serializable;

public class Rider implements Serializable {

    // Set in MainActivity
    private DestinationLocation mDestinationLocation;
    private String mTravelMode;
    private double mLatitude;
    private double mLongitude;

    // Set in MapViewFragment
    private boolean active;
    private boolean serviced;

    // Set in ParserTask
    private long mDestinationTime;
    private double mProximity;

    // Set in MainActivity when parsing Firebase
    private String firstName;
    private String lastName;
    private String companyID;
    private String uID;

    public Rider() {

    }

    public Rider(String firstName, String lastName, String uID, String companyID, boolean serviced) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.uID = uID;
        this.companyID = companyID;
        this.serviced = false;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
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

    public DestinationLocation getDestinationLocation() {
        return mDestinationLocation;
    }

    public void setDestinationLocation(DestinationLocation destinationLocation) {
        this.mDestinationLocation = destinationLocation;
    }

    public long getDestinationTime() {
        return mDestinationTime;
    }

    public void setDestinationTime(long destinationTime) {
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

    public String getTravelMode() {
        return mTravelMode;
    }

    public void setTravelMode(String mTravelMode) {
        this.mTravelMode = mTravelMode;
    }

    public boolean isServiced() {
        return serviced;
    }

    public void setServiced(boolean serviced) {
        this.serviced = serviced;
    }

    @Override
    public String toString() {
        String rider = "Name: " + firstName + "\n";
        rider += "UID: " + uID + "\n";
        rider += "Company ID: " + companyID + "\n";
        rider += "Active: " + active + "\n";
        rider += "TravelMode: " + mTravelMode;

        if (mDestinationLocation != null) {
            rider += mDestinationLocation.getDestinationName() + "\n";
        }

        return rider;
    }
}
