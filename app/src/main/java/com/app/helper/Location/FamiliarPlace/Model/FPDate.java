package com.app.helper.Location.FamiliarPlace.Model;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class FPDate implements Serializable {
    private String date;
    private String time_start;
    private String time_end;

    public FPDate(String date, String time_start, String time_end) {
        this.date = date;
        this.time_start = time_start;
        this.time_end = time_end;
    }

    public FPDate() {
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime_start() {
        return time_start;
    }

    public void setTime_start(String time_start) {
        this.time_start = time_start;
    }

    public String getTime_end() {
        return time_end;
    }

    public void setTime_end(String time_end) {
        this.time_end = time_end;
    }

    @NonNull
    @Override
    public String toString() {
        return "FPDate{" +
                "date='" + date + '\'' +
                ", time_start='" + time_start + '\'' +
                ", time_end='" + time_end + '\'' +
                '}';
    }

    public String getDateTimeStr(boolean isTimeEnd) {
        if (isTimeEnd) {
            return this.date + " " + this.time_end;
        }
        return this.date + " " + this.time_start;
    }

    public int[] convertDate() {
        int[] rs = new int[3];
        // 2021-08-25 (Thu 7)
        String date = this.date.split(" ")[0];
        String[] dateArr = date.split("-");
        rs[0] = Integer.parseInt(dateArr[0]);
        rs[1] = Integer.parseInt(dateArr[1]);
        rs[2] = Integer.parseInt(dateArr[2]);
        return rs;
    }

    public int[] convertTime(boolean isTimeStart) {
        int[] rs = new int[2];
        String[] timeArr;
        if (isTimeStart) {
            timeArr = this.time_start.split(":");
        } else {
            timeArr = this.time_end.split(":");
        }
        rs[0] = Integer.parseInt(timeArr[0]);
        rs[1] = Integer.parseInt(timeArr[1]);
        return rs;
    }
}
