package com.example.viktorjankov.shuttletracker.events;


public class TravelModeEvent {


    private String mTravelMode;

    public TravelModeEvent(String travelMode) {
        mTravelMode = travelMode;
    }

    public String getTravelSource() {
        return mTravelMode;
    }

    public void setTravelSource(String mTravelMode) {
        this.mTravelMode = mTravelMode;
    }
}
