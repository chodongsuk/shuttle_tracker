package com.viktorjankov.shuttletracker.singletons;

import com.viktorjankov.shuttletracker.model.Rider;

public class RiderProvider {

    private static Rider mRider;

    public static Rider getRider() {
        return mRider;
    }

    public static void setRider(Rider rider) {
        mRider = rider;
    }
}
