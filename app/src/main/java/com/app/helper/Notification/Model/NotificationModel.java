package com.app.helper.Notification.Model;

public class NotificationModel {
    private String to;
    private BaseData data;

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }


    public NotificationModel() {
    }

    public NotificationModel(String to, BaseData data) {
        this.to = to;
        this.data = data;
    }

    public BaseData getData() {
        return data;
    }

    public void setData(BaseData data) {
        this.data = data;
    }
}
