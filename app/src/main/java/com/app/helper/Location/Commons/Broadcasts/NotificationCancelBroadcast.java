package com.app.helper.Location.Commons.Broadcasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationManagerCompat;

public class NotificationCancelBroadcast extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            int code = intent.getIntExtra(MethodUtilsAlarmBroadcast.CODE, -1);
            if (code != -1) {
                // Cancel Alarm timeout
                MethodUtilsAlarmBroadcast.cancelAlarm(context, code);
                Log.e("NotificationCancelBroadcast", code + "");
            }
        }
    }
}
