package com.example.viktorjankov.shuttletracker.events;

import com.example.viktorjankov.shuttletracker.pickup_locations.DestinationLocation;

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
