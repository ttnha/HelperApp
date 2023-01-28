package com.app.helper.Views.Guardian;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;

import com.app.helper.R;
import com.app.helper.User.DAO.ListenersCallbacksDAO;
import com.app.helper.Notification.Service.NotificationService;
import com.app.helper.Utils.ViewUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class ConfirmCodeActivity extends AppCompatActivity {
    private TextView tv_time_s;
    private TextView tv_time;
    private Button btn_cancel, btn_confirm, btn_close;
    private EditText et_code;

    private final Activity activity = this;
    private Intent intent;

    private MyAsyncTask myAsyncTask;
    private boolean isTimeLimited;

    private int currentSecond;
    private String code;

    private ListenersCallbacksDAO listenersCallbacksDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_code);
        intent = getIntent();
        LocalDateTime time_limit = LocalDateTime.parse(intent.getStringExtra(NotificationService.TIME_LIMITED));
        LocalDateTime currentTime = LocalDateTime.now();
        isTimeLimited = currentTime.isAfter(time_limit);
        if (!isTimeLimited) {
            currentSecond = (int) currentTime.until(time_limit, ChronoUnit.SECONDS);
            code = intent.getStringExtra(NotificationService.CODE);
            setListenerCallbacks(code);
        }
        getWidgets();
        cancelNotification();
        setListeners();
        myAsyncTask.execute();
    }

    private void cancelNotification() {
        NotificationManagerCompat.from(getApplicationContext()).cancel(
                intent.getIntExtra(NotificationService.NOTIFICATION_ID, 0)
        );
    }

    private void setListenerCallbacks(String code) {
        listenersCallbacksDAO = new ListenersCallbacksDAO();
        listenersCallbacksDAO.checkStatusOnChange(code, new ListenersCallbacksDAO.IControlData() {
            @Override
            public void sendStatus(int status) {
                if (status > ListenersCallbacksDAO.STATUS_WAITING) {
                    if (status == ListenersCallbacksDAO.STATUS_CANCEL) {
                        Toast.makeText(activity, "Liên kết đã bị hủy", Toast.LENGTH_LONG).show();
                        listenersCallbacksDAO.removeListener();
                        myAsyncTask.cancel(true);
                        new Thread(() -> {
                            SystemClock.sleep(1000);
                            finish();
                        }).start();
                    }
                }
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void getWidgets() {
        myAsyncTask = new MyAsyncTask();
        RelativeLayout rl_main = findViewById(R.id.rl_main);
        RelativeLayout rl_limit = findViewById(R.id.rl_limit);
        if (isTimeLimited) {
            rl_limit.setVisibility(View.VISIBLE);
            rl_main.setVisibility(View.GONE);

            tv_time = findViewById(R.id.tv_time);
            btn_close = findViewById(R.id.btn_close);

        } else {
            tv_time_s = findViewById(R.id.tv_time_s);
            TextView tv_code = findViewById(R.id.tv_code);
            tv_code.setText("Nhập mã " + code + " để hoàn tất quá trình liên kết");

            btn_cancel = findViewById(R.id.btn_cancel);
            btn_confirm = findViewById(R.id.btn_confirm);

            et_code = findViewById(R.id.et_code);
        }
    }

    private void setListeners() {
        if (isTimeLimited) {
            btn_close.setOnClickListener(v -> finish());
        } else {
            btn_cancel.setOnClickListener(v -> {
                listenersCallbacksDAO.setStatus(ListenersCallbacksDAO.STATUS_CANCEL);
                finish();
            });
            btn_confirm.setOnClickListener(v -> {
                String code = et_code.getText().toString();
                if (TextUtils.isEmpty(code)) {
                    Toast.makeText(activity, "Vui lòng nhập mã liên kết!", Toast.LENGTH_LONG).show();
                } else {
                    if (!code.equals(this.code)) {
                        Toast.makeText(activity, "Mã liên kết chưa chính xác\nVui lòng thử lại!", Toast.LENGTH_LONG).show();
                    } else {
                        listenersCallbacksDAO.setStatus(ListenersCallbacksDAO.STATUS_ACCEPT);
                        Toast.makeText(activity, "Liên kết thành công", Toast.LENGTH_LONG).show();
                        ViewUtils.hideSoftKeyboard(et_code);
                        new Handler().postDelayed(this::finish, 1000);
                    }
                }
            });
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        myAsyncTask.cancel(true);
        listenersCallbacksDAO.removeListener();
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
            if (isTimeLimited) {
                for (int i = 10; i >= 0; i--) {
                    if (isCancelled())
                        break;
                    publishProgress(i);
                    SystemClock.sleep(1000);
                }
            } else {
                for (int i = currentSecond; i >= 0; i--) {
                    if (isCancelled())
                        break;
                    publishProgress(i);
                    SystemClock.sleep(1000);
                }
            }

            return null;
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            if (isTimeLimited) {
                tv_time.setText((values[0] > 9 ? values[0] : "0" + values[0]) + " giây");
            } else {
                tv_time_s.setText(String.valueOf(values[0] > 9 ? values[0] : "0" + values[0]));
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            listenersCallbacksDAO.removeListener();
            Toast.makeText(activity, "Hết thời gian", Toast.LENGTH_LONG).show();
            new Handler().postDelayed(activity::finish, 777);
        }
    }
}