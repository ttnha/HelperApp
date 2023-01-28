package com.app.helper.Views.Guardian;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.app.helper.Guardian.DAO.GuardianDAO;
import com.app.helper.R;
import com.app.helper.User.DAO.ListenersCallbacksDAO;
import com.app.helper.Notification.Service.NotificationService;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class RescuerActivity extends AppCompatActivity {
    private MediaPlayer catSoundMediaPlayer;
    private CircularProgressIndicator cpi;
    private TextView tv_time, tv_phone, tv_name;
    private MyAsyncTask myAsyncTask;
    private int currentSecond;
    private ListenersCallbacksDAO listenersCallbacksDAO;

    @RequiresApi(api = Build.VERSION_CODES.O_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rescuer);

        Intent intent = getIntent();
        String code = intent.getStringExtra(NotificationService.CODE);
        if (TextUtils.isEmpty(code))
            finish();
        LocalDateTime time_limit = LocalDateTime.parse(intent.getStringExtra(NotificationService.TIME_LIMITED));
        LocalDateTime currentTime = LocalDateTime.now();
        boolean isTimeLimited = currentTime.isAfter(time_limit);
        if (!isTimeLimited) {
            currentSecond = (int) currentTime.until(time_limit, ChronoUnit.SECONDS);
        } else {
            finish();
        }

        setupScreenLock();
        getWidgets();
        String name = intent.getStringExtra(NotificationService.NAME);
        String phone = intent.getStringExtra(NotificationService.UID);
        String latitude = intent.getStringExtra(NotificationService.LATITUDE);
        String longitude = intent.getStringExtra(NotificationService.LONGITUDE);

        setListenerCallbacks(code, name, phone);

        catSoundMediaPlayer = MediaPlayer.create(this, R.raw.sound);
        catSoundMediaPlayer.start();

        findViewById(R.id.rl_btn).setOnClickListener(v -> {
            listenersCallbacksDAO.setStatus(1);
            startStandardMap(latitude, longitude);
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O_MR1)
    private void setupScreenLock() {
        setShowWhenLocked(true);
        setTurnScreenOn(true);
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
    }

    private void getWidgets() {
        cpi = findViewById(R.id.cpi);
        tv_time = findViewById(R.id.tv_time);
        tv_phone = findViewById(R.id.tv_phone);
        tv_name = findViewById(R.id.tv_name);
    }

    @SuppressLint("QueryPermissionsNeeded")
    private void startStandardMap(String latitude, String longitude) {
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + latitude + "," + longitude + "&mode=w");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
            finish();
        }
    }

    private void setListenerCallbacks(String code, String name, String phone) {
        listenersCallbacksDAO = new ListenersCallbacksDAO();
        tv_name.setText(name);
        tv_phone.setText(phone);

        myAsyncTask = new MyAsyncTask();
        myAsyncTask.execute();

        listenersCallbacksDAO.checkStatusOnChange(code, new ListenersCallbacksDAO.IControlData() {
            @Override
            public void sendStatus(int status) {
                if (status != ListenersCallbacksDAO.STATUS_WAITING) {
                    Toast.makeText(RescuerActivity.this, "Bên kia đã dừng cuộc gọi", Toast.LENGTH_LONG).show();
                    listenersCallbacksDAO.removeValue();
                    listenersCallbacksDAO.removeListener();
                    if (!myAsyncTask.isCancelled())
                        myAsyncTask.cancel(true);
                    new Handler().postDelayed(() -> finish(), 1000);
                }
            }
        });
    }


    private void stopMedia() {
        if (catSoundMediaPlayer != null) {
            catSoundMediaPlayer.stop();
            catSoundMediaPlayer.release();
            catSoundMediaPlayer = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopMedia();
        if (myAsyncTask != null)
            if (!myAsyncTask.isCancelled()) myAsyncTask.cancel(true);
    }

    @SuppressLint("StaticFieldLeak")
    private class MyAsyncTask extends AsyncTask<Void, Integer, Void> {
        private final int TIME_CALL_LIMIT = GuardianDAO.TIME_LIMIT;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onCancelled(Void aVoid) {
            super.onCancelled(aVoid);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            for (int i = TIME_CALL_LIMIT - currentSecond; i <= TIME_CALL_LIMIT; i++) {
                if (isCancelled()) break;
                publishProgress(i);
                SystemClock.sleep(1000);
            }
            return null;
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            cpi.setProgressCompat(values[0], true);
            tv_time.setText(TIME_CALL_LIMIT - values[0] + "s");
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            finish();
        }
    }
}