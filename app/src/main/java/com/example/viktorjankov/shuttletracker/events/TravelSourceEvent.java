package com.example.viktorjankov.shuttletracker.events;


import com.example.viktorjankov.shuttletracker.travel_mode.TravelMode;

public class TravelSourceEvent {


    private TravelMode mTravelMode;

    public TravelSourceEvent(TravelMode travelMode) {
        mTravelMode = travelMode;
    }

    public TravelMode getTravelSource() {
        return mTravelMode;
    }

    public void setTravelSource(TravelMode mTravelMode) {
        this.mTravelMode = mTravelMode;
    }
}
