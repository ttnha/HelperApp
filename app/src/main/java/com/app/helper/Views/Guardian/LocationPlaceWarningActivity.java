package com.app.helper.Views.Guardian;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.app.helper.R;
import com.google.android.material.progressindicator.CircularProgressIndicator;

public class LocationPlaceWarningActivity extends AppCompatActivity {
    public static final String KEY_PLACE = "KEY_PLACE";
    public static final String KEY_FOLLOWED_NAME = "KEY_FOLLOWED_NAME";
    private MediaPlayer catSoundMediaPlayer;
    private CircularProgressIndicator cpi;
    private TextView tv_time, tv_place, tv_msg;
    private MyAsyncTask myAsyncTask;

    @RequiresApi(api = Build.VERSION_CODES.O_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lacation_place_warning);

        setupScreenLock();
        getWidgets();
        myAsyncTask = new MyAsyncTask();

        catSoundMediaPlayer = MediaPlayer.create(this, R.raw.sound);
        catSoundMediaPlayer.start();

        Intent intent = getIntent();
        String place = "Địa điểm: " + intent.getStringExtra(KEY_PLACE) + ".";
        String msg = intent.getStringExtra(KEY_FOLLOWED_NAME) + " hiện tại chưa đến vị trí này!!!";
        tv_place.setText(place);
        tv_msg.setText(msg);
        myAsyncTask.execute();
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
        tv_place = findViewById(R.id.tv_place);
        tv_msg = findViewById(R.id.tv_msg);
    }

    public void finish(View view) {
        finish();
    }

    private void stopMedia() {
        if (catSoundMediaPlayer != null) {
            catSoundMediaPlayer.stop();
            catSoundMediaPlayer.release();
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
            for (int i = 0; i <= 30; i++) {
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
            tv_time.setText(30 - values[0] + "s");
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            finish();
        }
    }
}