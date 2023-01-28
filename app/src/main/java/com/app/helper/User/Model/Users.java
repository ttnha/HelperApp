package com.app.helper.User.Model;

import com.app.helper.Guardian.Model.Guardian;

import java.io.Serializable;
import java.util.List;

public class Users implements Serializable {
    private String uid;
    private String pwd;
    private String name;
    private String token;
    private List<Guardian> guardians;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Guardian> getGuardians() {
        return guardians;
    }

    public void setGuardians(List<Guardian> guardians) {
        // Sắp xếp theo thú tự ưu tiên
        guardians.sort((o1, o2) -> o1.getPriority() - o2.getPriority());
        this.guardians = guardians;
    }
}
