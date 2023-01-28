package com.app.helper.Followed.Model;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class Followed implements Serializable {
    private String uid;
    private String name;

    public Followed() {
    }

    public Followed(String uid, String name) {
        this.uid = uid;
        this.name = name;
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

    @NonNull
    @Override
    public String toString() {
        return "Followed{" +
                "uid='" + uid + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
