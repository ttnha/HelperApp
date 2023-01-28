package com.app.helper.Location.Commons.Broadcasts;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationManagerCompat;

import com.app.helper.Location.FamiliarPlace.Model.FPDate;
import com.app.helper.Location.FamiliarPlace.Model.FamiliarPlace;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;

public class AlarmBroadcast extends BroadcastReceiver {

    private static final String KEY_DATA = "KEY_DATA";
    private static final String KEY_DATA_FOLLOWED = "KEY_DATA_FOLLOWED";
    private static final int DEFAULT_MINUTE = 3 * 60 * 1_000;

    @Override
    public void onReceive(Context context, Intent intent) {
        String msg = intent.getStringExtra(KEY_DATA);
        String followedName = intent.getStringExtra(KEY_DATA_FOLLOWED);
        if (msg != null) {
            FamiliarPlace familiarPlace = MethodUtilsAlarmBroadcast.convertJsonToObj(msg);
            LocalDateTime now = LocalDateTime.parse(LocalDateTime.now().format(MethodUtilsAlarmBroadcast.dft), MethodUtilsAlarmBroadcast.dft);
            LocalDateTime time_end = MethodUtilsAlarmBroadcast.convertStrToLDT(familiarPlace.getFp_date().getDateTimeStr(true));
            if (now.isBefore(time_end) || now.equals(time_end)) {
                MethodUtilsAlarmBroadcast.isInsideZoning(familiarPlace.getFp_date().getDate(), familiarPlace.getRadius(),
                        new LatLng(familiarPlace.getLatitude(), familiarPlace.getLongitude()),
                        familiarPlace.getId(),
                        new MethodUtilsAlarmBroadcast.IControlData() {
                            @Override
                            public void isInsideZoning(boolean is) {
                                if (!is) {
                                    // Gửi notification xác nhận, trong vòng 3 phút không xác nhận thì gọi điện
                                    MethodUtilsAlarmBroadcast.fireNotificationWarning(context, familiarPlace, followedName);
                                }
                                setAlarm(context, System.currentTimeMillis() + DEFAULT_MINUTE, familiarPlace, followedName);

                            }
                        });
            } else {
                // Sau thời gian đó, set thời gian là tuần sau báo lại
                if (familiarPlace.isIs_interval()) {
                    // Tính thời gian 1 tuần kể trừ ngày hiện tại
                    long timeNextWeek = LocalDateTime.now().plusWeeks(1).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(timeNextWeek);
//                    Log.e("TIME NEXT WEEK", calendar.toString());
                    FPDate fpDate = familiarPlace.getFp_date();
                    int monthInt = calendar.get(Calendar.MONTH) + 1;
                    String month = monthInt < 10 ? "0" + monthInt :
                            monthInt + "";
                    int dayInt = calendar.get(Calendar.DAY_OF_MONTH);
                    String day = dayInt < 10 ? "0" + dayInt :
                            dayInt + "";
                    fpDate.setDate(calendar.get(Calendar.YEAR) + "-" + month + "-" + day);
                    setAlarm(context, timeNextWeek, familiarPlace, followedName);
                }
            }

        }
    }

    public void setAlarm(Context context, long timeMillis, FamiliarPlace familiarPlace, String followedName) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmBroadcast.class);
        intent.putExtra(KEY_DATA, new Gson().toJson(familiarPlace));
        intent.putExtra(KEY_DATA_FOLLOWED, followedName);

        try {
            int id_al_br = Integer.parseInt(familiarPlace.getId());
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context, id_al_br, intent, PendingIntent.FLAG_UPDATE_CURRENT
            );
            Log.e("ID", id_al_br + "");
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                    timeMillis, pendingIntent);
        } catch (NumberFormatException e) {
            Log.e("MSG-ERR", e.getMessage());
        }

    }

    public void cancelAlarm(Context context, int alarm_id) {
//        Log.e("ID2",alarm_id+"");
//        Intent intent = new Intent(context, AlarmBroadcast.class);
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, alarm_id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//        alarmManager.cancel(pendingIntent);
//        // Cancel Notification
//        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(context);
//        managerCompat.cancel(alarm_id);
        MethodUtilsAlarmBroadcast.cancelAlarm(context, alarm_id);
    }


}
