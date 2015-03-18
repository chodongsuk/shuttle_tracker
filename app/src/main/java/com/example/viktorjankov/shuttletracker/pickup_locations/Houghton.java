package com.example.viktorjankov.shuttletracker.pickup_locations;

import com.google.android.gms.maps.model.LatLng;

public class Houghton implements PickupLocation {
    private double mLatitude;
    private double mLongitude;
    private LatLng mLocation;

    public Houghton(double lat, double lon)
    {
        mLatitude = lat;
        mLongitude = lon;
        mLocation = new LatLng(mLatitude, mLongitude);

    }

    public double getLatitude() {
        return mLatitude;
    }

    public void setLatitude(long mLatitude) {
        this.mLatitude = mLatitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public void setLongitude(long mLongitude) {
        this.mLongitude = mLongitude;
    }
}
