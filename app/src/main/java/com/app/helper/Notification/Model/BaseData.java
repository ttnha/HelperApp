package com.app.helper.Notification.Model;

public abstract class BaseData {
    protected int type;
    protected String code;
    protected String name;
    protected String uid;
    protected String time_limit;

    public BaseData(int type, String code, String name, String uid, String time_limit) {
        this.type = type;
        this.code = code;
        this.name = name;
        this.uid = uid;
        this.time_limit = time_limit;
    }

    public String getTime_limit() {
        return time_limit;
    }

    public void setTime_limit(String time_limit) {
        this.time_limit = time_limit;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
