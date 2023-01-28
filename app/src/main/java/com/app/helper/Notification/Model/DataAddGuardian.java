package com.app.helper.Notification.Model;

import com.app.helper.Notification.Service.NotificationService;

public class DataAddGuardian extends BaseData {
    private String content;

    public DataAddGuardian(String name, String content, String code, String uid, String time_limit) {
        super(NotificationService.TYPE_ADD_GUARDIAN, code, name, uid, time_limit);
        this.content = content;
    }


    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTime_limit() {
        return time_limit;
    }

    public void setTime_limit(String time_limit) {
        this.time_limit = time_limit;
    }
}
