package com.example.viktorjankov.shuttletracker.model;

import java.io.Serializable;

public class DestinationLocation implements Serializable {

    private String mDestinationName;
    private String mDestinationAddress;
    private double mLatitude;
    private double mLongitude;

    public DestinationLocation() {

    }
    public DestinationLocation(String destinationName, String destinationAddress, double lat, double lng) {
        mDestinationName = destinationName;
        mDestinationAddress = destinationAddress;
        mLatitude = lat;
        mLongitude = lng;
    }

    public String getDestinationAddress() {
        return mDestinationAddress;
    }

    public void setDestinationAddress(String mDestinationAddress) {
        this.mDestinationAddress = mDestinationAddress;
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
                "Destination Address: " + mDestinationAddress + "\n" +
               "Latitude: " + mLatitude + "\n" +
               "Longitude: " + mLongitude;
    }
}

