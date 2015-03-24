package com.example.viktorjankov.shuttletracker.travel_mode;

public class Car implements TravelMode {
    @Override
    public String getTravelMode() {
        return "driving";
    }
}
