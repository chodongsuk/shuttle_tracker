package com.example.viktorjankov.shuttletracker.model;

import com.google.android.gms.maps.model.LatLng;

public class DestinationLocation {

    private String mDestinationName;
    private LatLng mDestination;

    public DestinationLocation(String destinationName, double lat, double lng) {
        mDestinationName = destinationName;
        mDestination = new LatLng(lat, lng);
    }

    public String getDestinationName() {
        return mDestinationName;
    }

    public void setDestinationName(String mDestinationName) {
        this.mDestinationName = mDestinationName;
    }

    public LatLng getDestination() {
        return mDestination;
    }

    public void setDestination(LatLng mDestination) {
        this.mDestination = mDestination;
    }
}
