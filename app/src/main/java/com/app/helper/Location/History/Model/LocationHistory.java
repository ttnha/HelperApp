package com.app.helper.Location.History.Model;

import com.google.android.gms.maps.model.LatLng;

public class LocationHistory {
    private LatLng latLng;
    private String time;

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
