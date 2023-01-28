package com.app.helper.Location.Zoning.Model;

import androidx.annotation.NonNull;

import com.app.helper.Location.Commons.Model.MyLatLng;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class DataRequestLocationZoning {
    private String to;
    private Data data;

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "DataRequestLocationZoning{" +
                "to='" + to + '\'' +
                ", data=" + data +
                '}';
    }

    public static class Data {
        private String uid;
        private List<LatLng> polygon;
        private boolean is_notification;

        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = "\"" + uid + "\"";
        }

        public List<LatLng> getPolygon() {
            return polygon;
        }

        public void setPolygon(List<LatLng> polygon) {
            this.polygon = polygon;
        }

        public boolean isIs_notification() {
            return is_notification;
        }

        public void setIs_notification(boolean is_notification) {
            this.is_notification = is_notification;
        }

        @NonNull
        @Override
        public String toString() {
            return "Data{" +
                    "uid='" + uid + '\'' +
                    ", polygon=" + polygon +
                    ", is_notification=" + is_notification +
                    '}';
        }
    }
}

