package com.app.helper.Guardian.Model;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class Guardian implements Serializable {
    private String uid;
    private String name;
    private int priority;

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

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    @NonNull
    @Override
    public String toString() {
        return "Guardian{" +
                "uid='" + uid + '\'' +
                ", name='" + name + '\'' +
                ", priority=" + priority +
                '}';
    }
}
