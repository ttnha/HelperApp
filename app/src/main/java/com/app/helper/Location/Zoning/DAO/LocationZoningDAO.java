package com.app.helper.Location.Zoning.DAO;

import androidx.annotation.NonNull;

import com.app.helper.Firebases.FireBaseInit;
import com.app.helper.Firebases.TableName;
import com.app.helper.Location.Commons.Model.MyLatLng;
import com.app.helper.Location.Zoning.Model.DataRequestLocationZoning;
import com.app.helper.Location.Zoning.Model.LocationZoning;
import com.app.helper.Notification.Service.INotificationService;
import com.app.helper.User.Model.SessionManager;
import com.app.helper.User.Model.Users;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LocationZoningDAO {
    private static final String TABLE_NAME = TableName.LocationZoning.name();

    private final SessionManager session;

    public LocationZoningDAO() {
        session = SessionManager.getINSTANCE();
    }

    private String getCurrentUid() {
        Users currentUser = session.getUserSession();
        if (currentUser != null) return currentUser.getUid();
        return null;
    }

    public void getLocationZoning(String followedUid, IControlData iControlData) {
        String currentUid = getCurrentUid();
        if (currentUid != null) {
            FireBaseInit.getInstance().getReference()
                    .child(TABLE_NAME)
                    .child(followedUid)
                    .child(currentUid)
                    .get()
                    .addOnSuccessListener(dataSnapshot -> {
                        if (dataSnapshot.exists()) {
                            LocationZoning locationZoning = dataSnapshot.getValue(LocationZoning.class);
                            if (locationZoning != null) {
                                iControlData.locationZoning(locationZoning);
                            }
                        } else {
                            iControlData.locationZoning(null);
                        }
                    });
        } else {
            iControlData.locationZoning(null);
        }
    }

    public void putLocationZoning(String followedUid, LocationZoning locationZoning, IControlData iControlData) {
        String currentUid = getCurrentUid();
        if (currentUid != null) {
            if (locationZoning != null)  // else Xóa khoanh vùng
                locationZoning.setGuardian(currentUid);
            FireBaseInit.getInstance().getReference()
                    .child(TABLE_NAME)
                    .child(followedUid)
                    .child(currentUid)
                    .setValue(locationZoning)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Lấy token của người bị theo dõi để bắn notification qua
                            FireBaseInit.getInstance().getReference()
                                    .child(TableName.Users.name())
                                    .child(followedUid)
                                    .child("token")
                                    .get()
                                    .addOnSuccessListener(dataSnapshot -> {
                                        if (dataSnapshot.exists()) {
                                            String followed_token = String.valueOf(dataSnapshot.getValue());
                                            DataRequestLocationZoning dataRequest;
                                            if (locationZoning != null) {
                                                dataRequest = buildDataRequestNotification
                                                        (followed_token, currentUid, locationZoning.getPolygon(), locationZoning.isIs_notification());
                                            } else {
                                                dataRequest = buildDataRequestNotification
                                                        (followed_token, currentUid, null, false);
                                            }
                                            // Gửi notification đến máy người bị theo dõi (Followed)
                                            INotificationService
                                                    .INSTANCE_SINGLE
                                                    .sendNotification(dataRequest)
                                                    .enqueue(new Callback<Void>() {
                                                        @Override
                                                        public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                                                            iControlData.isOK(true);
                                                        }

                                                        @Override
                                                        public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                                                            iControlData.isOK(false);

                                                        }
                                                    });
                                        } else {
                                            iControlData.isOK(false);
                                        }
                                    });

                        } else {
                            iControlData.isOK(false);
                        }
                    });

        } else {
            iControlData.isOK(false);
        }
    }

    private DataRequestLocationZoning buildDataRequestNotification(String followed_token, String uid, List<MyLatLng> polygon, boolean is_notification) {
        DataRequestLocationZoning dataRequestLocationZoning = new DataRequestLocationZoning();
        dataRequestLocationZoning.setTo(followed_token);

        DataRequestLocationZoning.Data data = new DataRequestLocationZoning.Data();
        data.setIs_notification(is_notification);
        if (polygon != null)
            data.setPolygon(polygon.stream().map(v -> new LatLng(v.getLatitude(), v.getLongitude())).collect(Collectors.toList()));
        else
            data.setPolygon(null);

        data.setUid(uid);

        dataRequestLocationZoning.setData(data);

        return dataRequestLocationZoning;
    }

    public interface IControlData {
        default void locationZoning(LocationZoning locationZoning) {
        }

        default void isOK(boolean is) {
        }
    }
}
