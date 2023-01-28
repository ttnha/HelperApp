package com.app.helper.Views.Security;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.app.helper.R;
import com.app.helper.User.DAO.UserDAO;
import com.app.helper.User.Model.SessionManager;
import com.app.helper.Utils.ViewUtils;
import com.app.helper.Views.Main.MainActivity;
import com.app.helper.Views.Main.SplashScreenActivity;

public class LoginActivity extends AppCompatActivity {
    private EditText et_phone;
    private TextView tv_warning;
    private Button btn_next;
    private View clear_phone;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (SessionManager.getINSTANCE().getUserSession() != null) {
            if (SessionManager.getINSTANCE().isRememberPasswordFlag()) {
                UserDAO.loginWithRemember();
                startActivity(new Intent(this, MainActivity.class));
            }else
                startActivity(new Intent(this, LoginPwdActivity.class));

            finish();
        }
        setContentView(R.layout.activity_login);
        getWidgets();
        setListeners();
    }

    private void getWidgets() {
        et_phone = findViewById(R.id.et_phone);
        tv_warning = findViewById(R.id.tv_warning);
        btn_next = findViewById(R.id.btn_next);
        clear_phone = findViewById(R.id.clear_phone);
    }

    private void setListeners() {
        // Button
        btn_next.setOnClickListener(v -> {
            // Get phone
            String phoneNumber = et_phone.getText().toString();

            // Check !Empty
            if (TextUtils.isEmpty(phoneNumber)) {
                tv_warning.setVisibility(View.VISIBLE);
            } else {
                tv_warning.setVisibility(View.GONE);
                Intent intent = new Intent(LoginActivity.this, OTPActivity.class);
                intent.putExtra(getString(R.string.phone_number_otp), phoneNumber);
                startActivity(intent);
                finish();
            }

        });

        // EditText
        et_phone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s.toString())) {
                    clear_phone.setVisibility(View.INVISIBLE);
                    tv_warning.setVisibility(View.VISIBLE);
                    ViewUtils.showSoftKeyboard(et_phone);
                } else {
                    clear_phone.setVisibility(View.VISIBLE);
                    tv_warning.setVisibility(View.GONE);
                }
            }
        });

        // TextView
        clear_phone.setOnClickListener(v -> et_phone.setText(null));
    }
}