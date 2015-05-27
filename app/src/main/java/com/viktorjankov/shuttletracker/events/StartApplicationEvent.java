package com.viktorjankov.shuttletracker.events;

public class StartApplicationEvent {

    private String uuId;

    public StartApplicationEvent(String uuId) {
        this.uuId = uuId;
    }

    public String getUuId() {
        return uuId;
    }

    public void setUuId(String uuId) {
        this.uuId = uuId;
    }
}
