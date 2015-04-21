package com.example.viktorjankov.shuttletracker.model;

import java.io.Serializable;

public class TravelMode implements Serializable{
    String mTravelMode;

    public TravelMode(String travelMode) {
        mTravelMode = travelMode;
    }

    public String getTravelMode() {
        return mTravelMode;
    }

    public void setTravelMode(String mTravelMode) {
        this.mTravelMode = mTravelMode;
    }
}
