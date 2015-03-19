package com.example.viktorjankov.shuttletracker.events;


import com.example.viktorjankov.shuttletracker.travel_sources.TravelSource;

public class TravelSourceEvent {


    private TravelSource mTravelSource;

    public TravelSourceEvent(TravelSource travelSource) {
        mTravelSource = travelSource;
    }

    public TravelSource getTravelSource() {
        return mTravelSource;
    }

    public void setTravelSource(TravelSource mTravelSource) {
        this.mTravelSource = mTravelSource;
    }
}
