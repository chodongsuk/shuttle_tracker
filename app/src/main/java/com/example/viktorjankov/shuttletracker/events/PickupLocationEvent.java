package com.example.viktorjankov.shuttletracker.events;

import com.example.viktorjankov.shuttletracker.pickup_locations.PickupLocation;

public class PickupLocationEvent {
    PickupLocation mPickupLocation;

    public PickupLocationEvent(PickupLocation mPickupLocation) {
        this.mPickupLocation = mPickupLocation;
    }

    public PickupLocation getPickupLocation() {
        return mPickupLocation;
    }

    public void setPickupLocation(PickupLocation mPickupLocation) {
        this.mPickupLocation = mPickupLocation;
    }
}
