package com.app.helper.Location.Commons.DAO;

import android.content.Context;

import com.app.helper.Firebases.FireBaseInit;
import com.app.helper.Location.History.Model.LocationHistory;
import com.app.helper.Location.Commons.Model.MyLatLng;
import com.app.helper.User.Model.SessionManager;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.List;

public class LocationDAO {
    public static final String TABLE_NAME = "LocationHistory";
    private final SessionManager session;

    public LocationDAO() {
        session = SessionManager.getINSTANCE();
    }

    public LatLng getCurrentLocation() {
        return session.getLocationSession();
    }

    public void setRequestLocationBackground(boolean value) {
        session.setRequestLocationBackground(value);
    }

    public boolean isLocationBackgroundRequested() {
        return session.isLocationBackgroundRequested();
    }

    public void getLocationHistoryByDate(String uid, String date, IControlData iControlData) {
        FireBaseInit.getInstance().getReference().child(TABLE_NAME)
                .child(uid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        if (task.getResult().exists()) {
                            List<LocationHistory> locationHistoryList = new ArrayList<>();
                            boolean keyBreak = false;
                            for (DataSnapshot dataSnapshot : task.getResult().getChildren()) {
                                String key = dataSnapshot.getKey();
                                if (key != null) {
                                    String[] date_time = key.split(" "); // 2021-07-26 17:53:00
                                    if (date.equals(date_time[0])) {
                                        MyLatLng myLatLng = dataSnapshot.getValue(MyLatLng.class);
                                        if (myLatLng != null) {
                                            keyBreak = true;
                                            LocationHistory locationHistory = new LocationHistory();
                                            locationHistory.setTime(date_time[1]);
                                            locationHistory.setLatLng(myLatLng.toLatLng());
                                            locationHistoryList.add(locationHistory);
                                        }
                                    } else {
                                        if (keyBreak) break;
                                    }
                                }
                            }
                            iControlData.dataLocationHistory(locationHistoryList.isEmpty() ? null : locationHistoryList);
                            return;
                        }
                    }
                    iControlData.dataLocationHistory(null);
                });

    }

    public interface IControlData {
        default void dataLocationHistory(List<LocationHistory> locationHistoryList) {
        }
    }

}
