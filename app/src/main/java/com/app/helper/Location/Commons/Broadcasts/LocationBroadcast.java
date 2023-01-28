package com.app.helper.Location.Commons.Broadcasts;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.app.helper.Firebases.FireBaseInit;
import com.app.helper.Firebases.TableName;
import com.app.helper.Location.Commons.DAO.LocationDAO;
import com.app.helper.Location.Zoning.Model.DataRequestLocationZoning;
import com.app.helper.MyApplication.MyApplication;
import com.app.helper.Notification.Service.INotificationService;
import com.app.helper.R;
import com.app.helper.User.Model.SessionManager;
import com.app.helper.User.Model.Users;
import com.app.helper.Utils.UtilsClazz;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LocationBroadcast extends BroadcastReceiver {
    public static final String ACTION_PROCESS_UPDATES = "com.app.helper.Location.Commons.Broadcasts.LocationBroadcast.action" + ".PROCESS_UPDATES";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_PROCESS_UPDATES.equals(action)) {
                LocationResult result = LocationResult.extractResult(intent);
                if (result != null) {
                    Location location = result.getLastLocation();
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    postLocation(latLng);
                    processLocationZoning(latLng);
                    SessionManager.getINSTANCE().createOrUpdateLocationSession(latLng);
//                    sendNotification(context, latLng.latitude + " - " + latLng.longitude);
                }
            }
//            else if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
//                sendNotification(context, "ReBoot");
//                if (SessionManager.getINSTANCE().isLocationBackgroundRequested()) {
//                    new LocationHelper(context).requestLocationUpdates();
//                }
//            }
        }
    }

    private void processLocationZoning(LatLng latLng) {
        HashMap<String, DataRequestLocationZoning.Data> dataHashMap = SessionManager.getINSTANCE().getLocationZoningMap();
        if (dataHashMap != null && dataHashMap.size() != 0) {
            dataHashMap.forEach((key, value) -> {
                Log.e("processLocationZoning1", latLng.toString());
                Log.e("processLocationZoning2", key);
                Log.e("processLocationZoning2", value.getPolygon().toString());
                if (value.isIs_notification()) { // Nếu thông báo đang được bật
                    // Nếu ra khỏi khoanh vùng
                    Log.e("processLocationZoning2", "TRUE");
                    if (!PolyUtil.containsLocation(latLng, value.getPolygon(), true)) {
                        // Lấy token tương ứng với uid của mỗi người giám hộ
                        FireBaseInit.getInstance().getReference()
                                .child(TableName.Users.name())
                                .child(key)
                                .child("token")
                                .get()
                                .addOnSuccessListener(dataSnapshot -> {
                                    if (dataSnapshot.exists()) {
                                        String token = String.valueOf(dataSnapshot.getValue());
                                        // Tạo notification data
                                        Map<String, Object> dataRequest = new HashMap<>();
                                        dataRequest.put("to", token);
                                        Map<String, String> data = new HashMap<>();
                                        data.put("title", "Bạn có thông báo mới!!!");
                                        Users users = SessionManager.getINSTANCE().getUserSession();
                                        data.put("content", users.getName() + " - " + users.getUid() + "\nĐã ra khỏi khoanh vùng!!!");
                                        dataRequest.put("data", data);
                                        INotificationService.INSTANCE_SINGLE.sendNotification(dataRequest)
                                                .enqueue(new Callback<Void>() {
                                                    @Override
                                                    public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {

                                                    }

                                                    @Override
                                                    public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {

                                                    }
                                                });
                                    }
                                });
                    }
                }else{
                    Log.e("processLocationZoning2", "FALSE");
                }
            });
        }

    }

    private void postLocation(LatLng latLng) {
        Users users = SessionManager.getINSTANCE().getUserSession();
        if (users == null) return;
        String currentUid = users.getUid();
        FireBaseInit.getInstance().getReference()
                .child(LocationDAO.TABLE_NAME)
                .child(currentUid)
                .child(UtilsClazz.getCurrentDateTimeFormat())
                .setValue(latLng);
    }

    private void sendNotification(Context context, String text) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context, MyApplication.CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_location)
                        .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                                R.mipmap.ic_launcher_foreground))
                        .setContentTitle("Vị trí mới")
                        .setContentText(text)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setPriority(NotificationManager.IMPORTANCE_DEFAULT);
        notificationManager.notify(1998, notificationBuilder.build());
    }

}
