package com.app.helper.Views.Security;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.app.helper.R;
import com.app.helper.User.DAO.UserDAO;
import com.app.helper.User.Model.SessionManager;
import com.app.helper.Utils.ViewUtils;

public class OTPActivity extends AppCompatActivity {
    private final Activity activity = this;
    private EditText et_otp;
    private TextView tv_warning, tv_phone, tv_resend_otp, tv_change_phone, tv_otp_time;
    private Button btn_next;
    private View clear_otp;
    private LinearLayout ln_pbar;

    private String phoneNumber;

    private UserDAO userDAO;
    private SessionManager session;
    private MyAsyncTask myAsyncTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);
        this.phoneNumber = getPhoneNumber();
        if (TextUtils.isEmpty(phoneNumber)) return;
        getWidgets();

        myAsyncTask = new MyAsyncTask();

        userDAO = new UserDAO(this);
        userDAO.sendOTP(phoneNumber);

        myAsyncTask.execute();

        session = SessionManager.getINSTANCE();

        tv_phone.setText(this.phoneNumber);

        setListeners();
    }

    private String getPhoneNumber() {
        return getIntent().getExtras().getString(getString(R.string.phone_number_otp));
    }

    private void getWidgets() {
        et_otp = findViewById(R.id.et_otp);

        tv_warning = findViewById(R.id.tv_warning);
        tv_phone = findViewById(R.id.tv_phone);
        tv_resend_otp = findViewById(R.id.tv_resend_otp);
        tv_change_phone = findViewById(R.id.tv_change_phone);
        tv_otp_time = findViewById(R.id.tv_otp_time);

        btn_next = findViewById(R.id.btn_next);
        clear_otp = findViewById(R.id.clear_otp);

        ln_pbar = findViewById(R.id.ln_pbar);
    }

    private void setListeners() {
        // Button
        btn_next.setOnClickListener(v -> {
            String otpSession = session.getOTPSession();
            // Get OTP
            String otp = et_otp.getText().toString();

            // Check valid
            if (TextUtils.isEmpty(otp)) {
                tv_warning.setVisibility(View.VISIBLE);
            } else {
                tv_warning.setVisibility(View.GONE);
                if (!otp.equals(otpSession)) {
                    tv_warning.setVisibility(View.VISIBLE);
                } else {
                    tv_warning.setVisibility(View.GONE);
                    session.removeOTPSession();

                    ViewUtils.progressBarProcess(true, ln_pbar, activity);
                    // Kiểm tra thử số điện thoại này đã tồn tại chưa
                    // Nếu chưa thì chuyển qua trang đăng ký, else thì chuyển sang trang nhập mật khẩu
                    userDAO.isExistsPhoneNumber(phoneNumber, new UserDAO.IControlData() {
                        @Override
                        public void isSuccess(boolean is) {
                            Intent intent;
                            if (!is) {
                                intent = new Intent(activity, RegisterActivity.class);
                                intent.putExtra(getString(R.string.phone_number_otp), phoneNumber);
                            } else {
                                intent = new Intent(activity, LoginPwdActivity.class);
                            }
                            ViewUtils.progressBarProcess(false, ln_pbar, activity);

                            startActivity(intent);
                            finish();
                        }
                    });

                }

            }


        });

        // EditText
        et_otp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s.toString())) {
                    clear_otp.setVisibility(View.INVISIBLE);
                    tv_warning.setVisibility(View.VISIBLE);
                    ViewUtils.showSoftKeyboard(et_otp);
                } else {
                    clear_otp.setVisibility(View.VISIBLE);
                    tv_warning.setVisibility(View.GONE);
                }
            }
        });

        // TextView
        clear_otp.setOnClickListener(v -> et_otp.setText(null));

        tv_change_phone.setOnClickListener(v -> {
            startActivity(new Intent(activity, LoginActivity.class));
            finish();
        });

        tv_resend_otp.setOnClickListener(v -> {
            userDAO.sendOTP(phoneNumber);
            myAsyncTask = new MyAsyncTask();
            myAsyncTask.execute();
            Toast.makeText(activity, "Đã gửi OTP về số điện thoại " + phoneNumber, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (myAsyncTask.isCancelled())
            myAsyncTask.cancel(true);
    }

    @SuppressLint("StaticFieldLeak")
    private class MyAsyncTask extends AsyncTask<Void, Integer, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            tv_otp_time.setVisibility(View.VISIBLE);
            tv_resend_otp.setEnabled(false);
            tv_resend_otp.setTextColor(getColor(R.color.gray));
        }

        @Override
        protected void onCancelled(Void aVoid) {
            super.onCancelled(aVoid);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            for (int i = 59; i >= 0; i--) {
                if (isCancelled())
                    break;
                publishProgress(i);
                SystemClock.sleep(1000);
            }
            return null;
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            tv_otp_time.setText("(00:" + (values[0] > 9 ? values[0] : "0" + values[0]) + ")");
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            tv_otp_time.setVisibility(View.INVISIBLE);
            tv_resend_otp.setEnabled(true);
            tv_resend_otp.setTextColor(getColor(R.color.black));
        }
    }
}