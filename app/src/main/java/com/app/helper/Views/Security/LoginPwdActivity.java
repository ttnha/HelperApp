package com.app.helper.Views.Security;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.app.helper.R;
import com.app.helper.User.DAO.UserDAO;
import com.app.helper.User.Model.Users;
import com.app.helper.Utils.ViewUtils;
import com.app.helper.Views.Main.MainActivity;

public class LoginPwdActivity extends AppCompatActivity {
    private final Activity activity = this;
    private TextView tv_name, tv_phone, tv_out, tv_forgot_pwd, tv_warning, tv_remember;
    private EditText et_pass;
    private View clear_pass;
    private Button btn_login;
    private LinearLayout ln_pbar;
    private CheckBox cb_remember;

    private Users users;
    private UserDAO userDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_pwd);

        userDAO = new UserDAO(this);

        users = userDAO.getUserSession();
        if (users == null) {
            finish();
        } else {
            getWidgets();
            initDataSession();
            setListeners();
        }
    }

    private void getWidgets() {
        tv_name = findViewById(R.id.tv_name);
        tv_phone = findViewById(R.id.tv_phone);
        tv_out = findViewById(R.id.tv_out);
        tv_forgot_pwd = findViewById(R.id.tv_forgot_pwd);
        tv_warning = findViewById(R.id.tv_warning);
        tv_remember = findViewById(R.id.tv_remember);

        et_pass = findViewById(R.id.et_pass);
        clear_pass = findViewById(R.id.clear_pass);

        btn_login = findViewById(R.id.btn_login);

        ln_pbar = findViewById(R.id.ln_pbar);

        cb_remember = findViewById(R.id.cb_remember);
    }

    private void setListeners() {
        // TextView
        tv_remember.setOnClickListener(v -> cb_remember.setChecked(!cb_remember.isChecked()));
        tv_out.setOnClickListener(v -> {
            startActivity(new Intent(activity, LoginActivity.class));
            finish();
            userDAO.clearSession();
        });

        tv_forgot_pwd.setOnClickListener(v -> {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
            View view = activity.getLayoutInflater().inflate(R.layout.layout_forgot_password, null);
            TextView tv_close = view.findViewById(R.id.tv_close);
            CheckBox cb_agree = view.findViewById(R.id.cb_agree);
            Button btn_confirm = view.findViewById(R.id.btn_confirm);
            alertDialog.setView(view);
            final AlertDialog ad = alertDialog.create();
            ad.setCancelable(false);

            tv_close.setOnClickListener(e -> ad.dismiss());
            btn_confirm.setOnClickListener(e -> {
                boolean isAgree = cb_agree.isChecked();
                if (isAgree) {
                    userDAO.forgotPassword();
                    ad.dismiss();
                } else {
                    Toast.makeText(activity, "Bạn chưa chấp nhận yêu cầu hệ thống!!!", Toast.LENGTH_LONG).show();
                }
            });

            ad.show();
            Window window = ad.getWindow();
            WindowManager.LayoutParams param = window.getAttributes();
            param.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
            param.y = 200;
            window.setAttributes(param);
            window.setBackgroundDrawableResource(R.color.transparent);
        });

        clear_pass.setOnClickListener(v -> et_pass.setText(null));

        // EditText
        et_pass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s.toString())) {
                    ViewUtils.showSoftKeyboard(et_pass);
                    clear_pass.setVisibility(View.INVISIBLE);
                    tv_warning.setVisibility(View.VISIBLE);
                } else {
                    clear_pass.setVisibility(View.VISIBLE);
                    tv_warning.setVisibility(View.GONE);
                    // Auto login sau khi gõ 6 số
                    if (s.toString().length() == 6) {
                        handleLogin(s.toString());
                    }
                }
            }
        });

        // Button
        btn_login.setOnClickListener(v -> {
            // Get Pwd
            String pass = et_pass.getText().toString();

            // Check empty
            if (TextUtils.isEmpty(pass)) {
                tv_warning.setVisibility(View.VISIBLE);
            } else {
                // Check valid pwd
                handleLogin(pass);
            }


        });
    }

    private void handleLogin(String pass) {
        ViewUtils.progressBarProcess(true, ln_pbar, activity);
        ViewUtils.hideSoftKeyboard(activity);
        userDAO.login(pass, new UserDAO.IControlData() {
            @Override
            public void isSuccess(boolean is) {
                if (is) {
                    tv_warning.setVisibility(View.GONE);
                    startActivity(new Intent(activity, MainActivity.class));
                    if (cb_remember.isChecked()) {
                        userDAO.setRememberPasswordFlag(true);
                    }
                    finish();
                } else {
                    tv_warning.setVisibility(View.VISIBLE);
                }
                ViewUtils.progressBarProcess(false, ln_pbar, activity);
            }
        });
    }

    private void initDataSession() {
        tv_name.setText(users.getName());
        tv_phone.setText(users.getUid());
    }

}