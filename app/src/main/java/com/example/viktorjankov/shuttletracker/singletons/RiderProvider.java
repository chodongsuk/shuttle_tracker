package com.example.viktorjankov.shuttletracker.singletons;

import com.example.viktorjankov.shuttletracker.model.Rider;

public class RiderProvider {

    private static Rider mRider;

    public static Rider getRider() {
        if (mRider == null) {
            return new Rider();
        }
        return mRider;
    }

    public static void setRider(Rider rider) {
        mRider = rider;
    }
}
