package com.example.viktorjankov.shuttletracker.model;

public class DestinationLocation {

    private String mDestinationName;
    private double mLatitude;
    private double mLongitude;

    public DestinationLocation(String destinationName, double lat, double lng) {
        mDestinationName = destinationName;
        mLatitude = lat;
        mLongitude = lng;
    }

    public String getDestinationName() {
        return mDestinationName;
    }

    public void setDestinationName(String mDestinationName) {
        this.mDestinationName = mDestinationName;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public void setLatitude(double mLatitude) {
        this.mLatitude = mLatitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public void setLongitude(double mLongitude) {

        this.mLongitude = mLongitude;
    }


    @Override
    public String toString() {
        return "Destination Name: " + mDestinationName + "\n" +
               "Latitude: " + mLatitude + "\n" +
               "Longitude: " + mLongitude;
    }
}

