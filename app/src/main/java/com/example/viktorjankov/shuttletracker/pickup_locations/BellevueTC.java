package com.example.viktorjankov.shuttletracker.pickup_locations;

import com.google.android.gms.maps.model.LatLng;

public class BellevueTC implements PickupLocation {
    public static final String kLOG_TAG = BellevueTC.class.getSimpleName();
    private double mLatitude;
    private double mLongitude;
    private LatLng mLocation;

    public BellevueTC(double lat, double lon)
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
