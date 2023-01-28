package com.app.helper.Views.Guardian;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.helper.Guardian.DAO.GuardianDAO;
import com.app.helper.R;
import com.app.helper.User.DAO.UserDAO;
import com.app.helper.User.Model.Users;
import com.app.helper.Views.Adapters.RV_GuardiansAdapter;

public class GuardiansActivity extends AppCompatActivity {
    private final Activity activity = this;
    private TextView tv_name, tv_phone;
    private ImageView iv_home;
    private RecyclerView rv_guardians;

    private Users users;
    private GuardianDAO guardianDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        users = UserDAO.getUsersSession();
        if (users == null) finish();
        setContentView(R.layout.activity_guardians);
        // Model
        guardianDAO = new GuardianDAO();
        // View
        getWidgets();
        initGuardianList();
        initDataSession();
        setListeners();
    }

    private void getWidgets() {
        tv_name = findViewById(R.id.tv_name);
        tv_phone = findViewById(R.id.tv_phone);

        iv_home = findViewById(R.id.iv_home);

        rv_guardians = findViewById(R.id.rv_guardians);

    }

    private void setListeners() {
        // ImageView
        iv_home.setOnClickListener(v -> finish());
    }

    private void initDataSession() {
        tv_name.setText(users.getName());
        tv_phone.setText(users.getUid());
    }

    private void initGuardianList() {
        rv_guardians.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        RV_GuardiansAdapter rv_guardiansAdapter = new RV_GuardiansAdapter(activity);
        rv_guardiansAdapter.setGuardianList(guardianDAO.getGuardianList());

        rv_guardiansAdapter.setItemTouchHelper(rv_guardians);

        rv_guardians.setAdapter(rv_guardiansAdapter);
    }


}