package com.example.viktorjankov.shuttletracker.model;

import com.google.android.gms.maps.model.LatLng;

public class User {

    private String userName;
    private LatLng location;
    private boolean active;


    public User(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}

