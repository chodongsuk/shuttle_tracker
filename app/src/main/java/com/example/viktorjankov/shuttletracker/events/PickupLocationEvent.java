package com.example.viktorjankov.shuttletracker.events;

import com.example.viktorjankov.shuttletracker.model.DestinationLocation;

public class PickupLocationEvent {
    DestinationLocation mDestinationLocation;

    public PickupLocationEvent(DestinationLocation mDestinationLocation) {
        this.mDestinationLocation = mDestinationLocation;
    }

    public DestinationLocation getPickupLocation() {
        return mDestinationLocation;
    }

    public void setPickupLocation(DestinationLocation mDestinationLocation) {
        this.mDestinationLocation = mDestinationLocation;
    }
}
