package com.example.viktorjankov.shuttletracker.pickup_locations;

import com.google.android.gms.maps.model.LatLng;

public interface DestinationLocation {

    public LatLng getLatLong();
    public String getLocationName();
}
