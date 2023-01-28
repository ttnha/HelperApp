package com.app.helper.Location.Zoning.Model;

import androidx.annotation.NonNull;

import com.app.helper.Location.Commons.Model.MyLatLng;
import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

public class LocationZoning implements Serializable {
    private String guardian;
    private boolean is_notification = true;
    private List<MyLatLng> polygon;

    public String getGuardian() {
        return guardian;
    }

    public void setGuardian(String guardian) {
        this.guardian = guardian;
    }

    public boolean isIs_notification() {
        return is_notification;
    }

    public void setIs_notification(boolean is_notification) {
        this.is_notification = is_notification;
    }

    public List<MyLatLng> getPolygon() {
        return polygon;
    }

    public void setPolygon(List<MyLatLng> polygon) {
        this.polygon = polygon;
    }

    @NonNull
    @Override
    public String toString() {
        return "LocationZoning{" +
                "guardian='" + guardian + '\'' +
                ", is_notification=" + is_notification +
                ", polygon=" + polygon +
                '}';
    }

    public List<LatLng> convertList() {
        if (this.polygon != null) {
            return this.polygon.stream().map(v -> new LatLng(v.getLatitude(), v.getLongitude())
            ).collect(Collectors.toList());
        } else {
            return null;
        }
    }
}
