package com.app.helper.Location.Commons.Broadcasts;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.app.helper.Firebases.FireBaseInit;
import com.app.helper.Firebases.TableName;
import com.app.helper.Location.Commons.Model.MyLatLng;
import com.app.helper.Location.FamiliarPlace.Model.FamiliarPlace;
import com.app.helper.MyApplication.MyApplication;
import com.app.helper.R;
import com.app.helper.User.Model.SessionManager;
import com.app.helper.Utils.UtilsClazz;
import com.app.helper.Views.Guardian.LocationPlaceWarningActivity;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.gson.Gson;
import com.google.maps.android.SphericalUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

class MethodUtilsAlarmBroadcast {
    public static final DateTimeFormatter dft = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final int DEFAULT_TIMEOUT = 90 * 1_000; // 1p30s

    static void isInsideZoning(String date, double radius, LatLng centerPoint, String fpId, IControlData iControlData) {
        String followId = SessionManager.getINSTANCE().getFollowIdByFPID(fpId);
        if (followId != null) {
            // Lấy vị trí cuối cùng của thằng followId
            FireBaseInit.getInstance().getReference()
                    .child(TableName.LocationHistory.name())
                    .child(followId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (task.getResult() != null && task.getResult().exists()) {
                                MyLatLng lastLng = null;
                                for (DataSnapshot ds : task.getResult().getChildren()) {
                                    if (ds.getKey() != null) {
                                        if (ds.getKey().contains(date))
                                            lastLng = ds.getValue(MyLatLng.class);
                                    }
                                }
                                if (lastLng != null) {
                                    LatLng latLng = lastLng.toLatLng();
                                    double distance = SphericalUtil.computeDistanceBetween(centerPoint, latLng);
                                    iControlData.isInsideZoning(distance <= radius);
                                } else {
                                    iControlData.isInsideZoning(false);
                                }
                            }
                        } else {
                            iControlData.isInsideZoning(false);
                        }
                    });
        }
    }

    static void fireNotificationWarning(Context context, FamiliarPlace familiarPlace, String followedName) {

//        int code = Integer.parseInt(UtilsClazz.random6Code());
        try {
            int noti_id = Integer.parseInt(familiarPlace.getId());

            StringBuilder msg = new StringBuilder("-> Người dùng: ");
            msg.append(followedName).append(" hiện tại chưa tới địa điểm này!!!")
                    .append("\n")
                    .append("-> Thông tin địa điểm:").append("\n")
                    .append("+ Tên: ").append(familiarPlace.getPlace_name()).append("\n")
                    .append("+ Thời gian: ngày ").append(familiarPlace.getFp_date().getDate()).append(", từ ").append(familiarPlace.getFp_date().getTime_start()).append(" đến ").append(familiarPlace.getFp_date().getTime_end()).append("\n");
//        String message = "-> Địa điểm: " + familiarPlace.getPlace_name() + "\n-> " + followedName + " hiện tại chưa có mặt tại địa điểm này!!!";
            // Set timeout 3 phút, nếu không nhấn OK thì gọi điện
            makeAlarmTimeout(context, noti_id, familiarPlace.getPlace_name(), followedName);
            // Gửi thông báo cho giám hộ
            makeNotificationWarning(context, msg.toString(), noti_id);
        } catch (NumberFormatException e) {
            Log.e("MSG-ERR", e.getMessage());
        }


    }


    private static void makeAlarmTimeout(Context context, int code, String place, String followedName) {
        Intent intent = new Intent(context, LocationPlaceWarningActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(LocationPlaceWarningActivity.KEY_PLACE, place);
        intent.putExtra(LocationPlaceWarningActivity.KEY_FOLLOWED_NAME, followedName);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, code, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        mgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + DEFAULT_TIMEOUT, pendingIntent);

    }

    public static void cancelAlarm(Context context, int alarm_id) {
        Log.e("ID2", alarm_id + "");
        Intent intent = new Intent(context, LocationPlaceWarningActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, alarm_id, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);

        // Cancel Notification
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(context);
        managerCompat.cancel(alarm_id);
    }

    public static final String CODE = "code";

    private static void makeNotificationWarning(Context context, String message, int code) {
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Intent cancel = new Intent(context, NotificationCancelBroadcast.class);
        cancel.putExtra(CODE, code);
        PendingIntent intentAccept = PendingIntent.getBroadcast(context, code, cancel, 0);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context, MyApplication.CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_baseline_warning_24)
                        .setColor(context.getColor(R.color.red))
                        .setContentTitle("Thông báo ĐỊA ĐIỂM QUEN THUỘC!!!")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(message))
                        .setSound(defaultSoundUri)
                        .setAutoCancel(false)
                        .setTimeoutAfter(DEFAULT_TIMEOUT)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setPriority(NotificationManager.IMPORTANCE_DEFAULT)
                        .setContentIntent(intentAccept)
                        .addAction(R.mipmap.ic_launcher_round, "XÁC NHẬN", intentAccept);
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(context);
        managerCompat.notify(code, notificationBuilder.build());
    }

    static FamiliarPlace convertJsonToObj(String json) {
        return new Gson().fromJson(json, FamiliarPlace.class);
    }

    static LocalDateTime convertStrToLDT(String date) {
        return LocalDateTime.parse(date, dft);
    }

    interface IControlData {
        default void isInsideZoning(boolean is) {
        }
    }
}
