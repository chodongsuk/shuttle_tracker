package com.example.viktorjankov.shuttletracker.model;

import java.io.Serializable;

public class Rider implements Serializable {

    // Set in MainActivity
    private DestinationLocation mDestinationLocation;
    private TravelMode mTravelMode;
    private double mLatitude;
    private double mLongitude;

    // Set in MapViewFragment
    private boolean active;

    // Set in ParserTask
    private String mDestinationTime;
    private double mProximity;

    // Set in MainActivity when parsing Firebase
    private String firstName;
    private String uID;
    private String companyID;

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

    public String getDestinationTime() {
        return mDestinationTime;
    }

    public void setDestinationTime(String destinationTime) {
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

    public TravelMode getTravelMode() {
        return mTravelMode;
    }

    public void setTravelMode(TravelMode mTravelMode) {
        this.mTravelMode = mTravelMode;
    }

    @Override
    public String toString() {
        String rider = "Name: " + firstName + "\n";
        rider += "Active: " + active + "\n";

        if (mDestinationLocation != null) {
            rider += mDestinationLocation.getDestinationName() + "\n";
        }

        if (mTravelMode != null) {
            rider += mTravelMode.getTravelMode() + "\n";
        }
        return rider;
    }
}
