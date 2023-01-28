package com.app.helper.Notification.Model;

import com.app.helper.Notification.Service.NotificationService;

public class DataCallGuardian extends BaseData {
    private double longitude;
    private double latitude;

    public DataCallGuardian(String code, String name, String uid,String time_limit) {
        super(NotificationService.TYPE_CALL_GUARDIAN, code, name, uid,time_limit);
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
}
