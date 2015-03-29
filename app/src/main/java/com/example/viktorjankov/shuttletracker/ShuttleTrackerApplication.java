package com.example.viktorjankov.shuttletracker;

import android.app.Application;

import com.firebase.client.Firebase;

public class ShuttleTrackerApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
    }
}
