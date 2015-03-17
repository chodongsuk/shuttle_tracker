package com.example.viktorjankov.shuttletracker.pickup_locations;

public class SouthKirkland implements PickUpDestination  {

    private double mLatitude;
    private double mLongitude;

    public SouthKirkland(double lat, double lon)
    {
        mLatitude = lat;
        mLongitude = lon;
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
