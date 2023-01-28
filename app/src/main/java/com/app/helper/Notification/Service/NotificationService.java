package com.app.helper.Notification.Service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.app.helper.Location.Zoning.Model.DataRequestLocationZoning;
import com.app.helper.MyApplication.MyApplication;
import com.app.helper.R;
import com.app.helper.User.Model.SessionManager;
import com.app.helper.Utils.UtilsClazz;
import com.app.helper.Views.Guardian.ConfirmCodeActivity;
import com.app.helper.Views.Guardian.RescuerActivity;
import com.app.helper.Views.Security.LoginActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import java.util.Map;
import java.util.Random;

public class NotificationService extends FirebaseMessagingService {
    public static final int TYPE_ADD_GUARDIAN = 0; // Chỉ hiện thông báo
    public static final int TYPE_CALL_GUARDIAN = 1; // Hiện màn hình khẩn cấp cho người giám hộ được gọi

    public static final String NOTIFICATION_ID = "NOTIFICATION_ID";
    public static final String CODE = "code";
    public static final String TIME_LIMITED = "time_limit";
    public static final String NAME = "name";
    public static final String UID = "uid";
    public static final String LONGITUDE = "longitude";
    public static final String LATITUDE = "latitude";
    public static final String CONTENT = "content";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
//        Log.e("onMessageReceived", remoteMessage.toString());
        Map<String, String> dataMsg = remoteMessage.getData();

        if (dataMsg.containsKey("type")) {
            String typeStr = dataMsg.get("type");
            if (typeStr != null) { // Gửi thông báo thêm giám hộ, gọi điện giám hộ khi nhấn SOS
                int type = Integer.parseInt(typeStr);
                if (type == TYPE_ADD_GUARDIAN) {
                    sendNotificationAddGuardian(dataMsg);
                } else if (type == TYPE_CALL_GUARDIAN) {
                    sendNotificationCallGuardian(dataMsg);
                }
            }
        } else {
            if (dataMsg.containsKey("is_notification")) { // Gửi thông báo cập nhật khoanh vùng vào Session
                try {
                    DataRequestLocationZoning.Data data = new Gson().fromJson(dataMsg.toString(), DataRequestLocationZoning.Data.class);
                    SessionManager.getINSTANCE().createOrUpdateLocationZoningMap(data);


                } catch (Exception e) {
                    Log.e("EX", e.getMessage());
                }
            } else {
                sendNotificationLocationZoning(dataMsg);
            }

        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        SessionManager.getINSTANCE().createTokenSession(token);
    }

    private void sendNotificationAddGuardian(Map<String, String> data) {
        try {
            String code = data.get(CODE);
            String time_limited = data.get(TIME_LIMITED);
            String name = data.get(NAME);
            String content = data.get(CONTENT);

            int notificationId = new Random().nextInt();

            String title = "Chào " + name + "! Bạn có thông báo mới!";

            Intent intent = new Intent(this, ConfirmCodeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.putExtra(NOTIFICATION_ID, notificationId);
            intent.putExtra(CODE, code);
            intent.putExtra(TIME_LIMITED, time_limited);

            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(this, MyApplication.CHANNEL_ID)
                            .setSmallIcon(R.mipmap.ic_launcher_foreground)
                            .setContentTitle(title)
                            .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                            .setAutoCancel(true)
                            .setTimeoutAfter(1000 * 60)
                            .setDefaults(Notification.DEFAULT_ALL)
                            .setPriority(NotificationManager.IMPORTANCE_DEFAULT);
            notificationBuilder.setContentIntent(pendingIntent);
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
            notificationManager.notify(notificationId, notificationBuilder.build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendNotificationCallGuardian(Map<String, String> data) {
        String name = data.get(NAME);
        String code = data.get(CODE);
        String uid = data.get(UID);
        String time_limited = data.get(TIME_LIMITED);
        String latitude = data.get(LATITUDE);
        String longitude = data.get(LONGITUDE);

        Intent intent = new Intent(this, RescuerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.putExtra(CODE, code);
        intent.putExtra(NAME, name);
        intent.putExtra(UID, uid);
        intent.putExtra(TIME_LIMITED, time_limited);
        intent.putExtra(LATITUDE, latitude);
        intent.putExtra(LONGITUDE, longitude);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager mgr = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);

        mgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pendingIntent);
    }

    private void sendNotificationLocationZoning(Map<String, String> data) {
        try {
            String title = data.get("title");
            String content = data.get(CONTENT);

            int notificationId = Integer.parseInt(UtilsClazz.random6Code());

            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);

            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(this, MyApplication.CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_baseline_warning_24)
                            .setColor(Color.RED)
                            .setContentTitle(title)
                            .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                            .setAutoCancel(true)
                            .setDefaults(Notification.DEFAULT_ALL)
                            .setPriority(NotificationManager.IMPORTANCE_HIGH);
            notificationBuilder.setContentIntent(pendingIntent);
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
            notificationManager.notify(notificationId, notificationBuilder.build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
