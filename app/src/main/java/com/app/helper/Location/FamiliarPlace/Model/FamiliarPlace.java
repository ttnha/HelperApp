package com.app.helper.Location.FamiliarPlace.Model;

import androidx.annotation.NonNull;

import com.app.helper.Utils.UtilsClazz;

import java.io.Serializable;

public class FamiliarPlace implements Serializable {
    private final String id;
    private String place_name;
    private double latitude;
    private double longitude;
    private FPDate fp_date;
    private boolean is_interval;
    private int radius;
    private boolean status = true;

    public FamiliarPlace() {
        this.id = UtilsClazz.random6Code();
    }

    public FamiliarPlace(double latitude, double longitude, String place_name, int radius, boolean status, FPDate fp_date, boolean is_interval) {
        this.id = UtilsClazz.random6Code();
        this.latitude = latitude;
        this.longitude = longitude;
        this.place_name = place_name;
        this.radius = radius;
        this.status = status;
        this.fp_date = fp_date;
        this.is_interval = is_interval;
    }

    public boolean isIs_interval() {
        return is_interval;
    }

    public void setIs_interval(boolean is_interval) {
        this.is_interval = is_interval;
    }

    public FPDate getFp_date() {
        if (this.fp_date == null) fp_date = new FPDate();
        return fp_date;
    }

    public void setFp_date(FPDate fp_date) {
        this.fp_date = fp_date;
    }

    public String getId() {
        return id;
    }

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

    public String getPlace_name() {
        return place_name;
    }

    public void setPlace_name(String place_name) {
        this.place_name = place_name;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    @NonNull
    @Override
    public String toString() {
        return "FamiliarPlace{" +
                "id='" + id + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", place_name='" + place_name + '\'' +
                ", radius=" + radius +
                ", status=" + status +
                ", fp_date=" + fp_date +
                ", is_interval=" + is_interval +
                '}';
    }
}
