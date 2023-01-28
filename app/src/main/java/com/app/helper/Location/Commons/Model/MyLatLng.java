package com.app.helper.Location.Commons.Model;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

public final class MyLatLng implements Serializable {
    private double latitude;
    private double longitude;

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public LatLng toLatLng() {
        return new LatLng(this.latitude, this.longitude);
    }

    public MyLatLng() {
    }

    public MyLatLng(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return "{" +
                "\"latitude\"=" + latitude +
                ", \"longitude\"=" + longitude +
                '}';
    }
}
