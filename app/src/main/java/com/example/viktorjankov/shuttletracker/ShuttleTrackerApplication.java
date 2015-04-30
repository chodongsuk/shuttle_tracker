package com.example.viktorjankov.shuttletracker;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.example.viktorjankov.shuttletracker.fragments.MapViewFragment;
import com.firebase.client.Firebase;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class ShuttleTrackerApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Roboto-Thin.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());
    }
}
