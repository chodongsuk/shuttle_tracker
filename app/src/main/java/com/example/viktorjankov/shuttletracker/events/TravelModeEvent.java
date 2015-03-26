package com.example.viktorjankov.shuttletracker.events;


import com.example.viktorjankov.shuttletracker.model.TravelMode;

public class TravelModeEvent {


    private TravelMode mTravelMode;

    public TravelModeEvent(TravelMode travelMode) {
        mTravelMode = travelMode;
    }

    public TravelMode getTravelSource() {
        return mTravelMode;
    }

    public void setTravelSource(TravelMode mTravelMode) {
        this.mTravelMode = mTravelMode;
    }
}
