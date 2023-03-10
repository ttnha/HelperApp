package com.app.helper.Views.Main;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.app.helper.Guardian.DAO.GuardianDAO;
import com.app.helper.Guardian.Model.Guardian;
import com.app.helper.Location.Commons.DAO.LocationDAO;
import com.app.helper.Location.History.Model.LocationHelper;
import com.app.helper.Notification.Model.DataCallGuardian;
import com.app.helper.R;
import com.app.helper.User.DAO.FeedbackDAO;
import com.app.helper.User.DAO.ListenersCallbacksDAO;
import com.app.helper.User.DAO.UserDAO;
import com.app.helper.User.Model.Users;
import com.app.helper.Utils.UtilsClazz;
import com.app.helper.Utils.ViewUtils;
import com.app.helper.Views.Followed.FollowActivity;
import com.app.helper.Views.Guardian.GuardiansActivity;
import com.app.helper.Views.Security.LoginActivity;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private final Activity activity = this;
    private TextView tv_name, tv_phone;
    private ImageView iv_logout;
    private LinearLayout ln_guardians, ln_follow, ln_setting, ln_feedback, ln_sos;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;

    private UserDAO userDAO;
    private FeedbackDAO feedbackDAO;
    private GuardianDAO guardianDAO;
    private LocationDAO locationDAO;
    private ListenersCallbacksDAO listenersCallbacksDAO;

    private Users currentUser;

    private LocationHelper locationHelper;

    @Override
    protected void onResume() {
        super.onResume();
        currentUser = userDAO.getUserSession();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        userDAO = new UserDAO(activity);
        currentUser = userDAO.getUserSession();
        if (currentUser == null) finish();
        if (userDAO.isForgotPasswordFlag()) {
            showDialogChangePassword(null);
        }
        guardianDAO = new GuardianDAO();
        listenersCallbacksDAO = new ListenersCallbacksDAO();

        locationDAO = new LocationDAO();
        feedbackDAO = new FeedbackDAO();
        locationHelper = new LocationHelper(this);
        locationHelper.requestLocationUpdates();

        getWidget();
        initDataSession();
        setListeners();

        if (!ViewUtils.isOnGPSProvider(activity)) {
            ViewUtils.showSettingsGPS(activity);
        }
    }

    private void getWidget() {
        drawerLayout = findViewById(R.id.dl);
        navigationView = findViewById(R.id.nv);

        tv_name = findViewById(R.id.tv_name);
        tv_phone = findViewById(R.id.tv_phone);

        ln_guardians = findViewById(R.id.ln_guardians);
        ln_follow = findViewById(R.id.ln_follow);
        ln_setting = findViewById(R.id.ln_setting);
        ln_feedback = findViewById(R.id.ln_feedback);

        ln_sos = findViewById(R.id.ln_sos);

        iv_logout = findViewById(R.id.iv_logout);
    }

    @SuppressLint("NonConstantResourceId")
    private void setListeners() {
        // ImaView
        iv_logout.setOnClickListener(v -> {
            finish();
            userDAO.removeEventListener();
        });

        // Main action
        View.OnClickListener onClickListener = v -> {
            switch (v.getId()) {
                case R.id.ln_guardians:
                    startActivity(new Intent(activity, GuardiansActivity.class));
                    break;
                case R.id.ln_follow:
                    startActivity(new Intent(activity, FollowActivity.class));
                    break;
                case R.id.ln_setting:
                    drawerLayout.openDrawer(GravityCompat.START, true);
                    break;
                default: //ln_feedback
                    showDialogFeedback();
                    break;
            }
        };
        ln_guardians.setOnClickListener(onClickListener);
        ln_follow.setOnClickListener(onClickListener);
        ln_setting.setOnClickListener(onClickListener);
        ln_feedback.setOnClickListener(onClickListener);

        // Navigation
        navigationView.setNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.i_war:
                    showDialogWar();
                    break;
                case R.id.i_pass:
                    // Build Dialog Change Password
                    showDialogChangePassword(item);
                    break;
                case R.id.i_finish:
                    drawerLayout.closeDrawer(GravityCompat.START, true);
                    userDAO.removeEventListener();
                    finish();
                    break;
                default:
                    drawerLayout.closeDrawer(GravityCompat.START, true);
                    locationHelper.removeLocationUpdates();
                    userDAO.clearSession();
                    userDAO.removeEventListener();
                    startActivity(new Intent(activity, LoginActivity.class));
                    finish();
                    break;
            }
            return true;
        });
        // SOS
        ln_sos.setOnClickListener(v -> {
            List<Guardian> guardianList = currentUser.getGuardians();
            if (guardianList == null || guardianList.isEmpty()) {
                new MaterialAlertDialogBuilder(activity).setCancelable(false).
                        setMessage("B???n ch??a th??m ai l??m gi??m h???!")
                        .setPositiveButton("Th??m", (dialog, which) -> {
                            dialog.dismiss();
                            startActivity(new Intent(activity, GuardiansActivity.class));
                        })
                        .setNegativeButton("????ng", (dialog, which) -> dialog.dismiss())
                        .show();
            } else {
                if (ViewUtils.isOnGPSProvider(activity)) {
                    if (locationDAO.getCurrentLocation() != null)
                        showDialogCalling(guardianList);
                    else
                        Toast.makeText(activity, "??ang c???p nh???t v??? tr?? hi???n t???i\nVui l??ng th??? l???i!", Toast.LENGTH_LONG).show();
                } else {
                    ViewUtils.showSettingsGPS(activity);
                }
            }
        });
    }

    private void showDialogWar() {
        new MaterialAlertDialogBuilder(activity)
                .setPositiveButton("????ng", (dialog, which) -> {
                    dialog.dismiss();
                })
        .setTitle("????? ???ng d???ng ho???t ?????ng t???t h??n")
        .setMessage("1. C???p quy???n v??? tr?? lu??n cho ph??p\n2. Cho ph??p ???ng d???ng xu???t hi???n tr??n c??ng")
        .show();
    }

    private void showDialogFeedback() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
        @SuppressLint("InflateParams")
        View view = activity.getLayoutInflater().inflate(R.layout.layout_feedback, null);
        TextInputLayout til_msg = view.findViewById(R.id.til_msg);
        RelativeLayout rl_pbar = view.findViewById(R.id.rl_pbar);
        Button btn_send = view.findViewById(R.id.btn_send);
        Button btn_cancel = view.findViewById(R.id.btn_cancel);
        alertDialog.setView(view);
        final AlertDialog ad = alertDialog.create();
        ad.setCancelable(false);

        // Listeners
        btn_cancel.setOnClickListener(v -> ad.dismiss());
        Objects.requireNonNull(til_msg.getEditText()).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s.toString())) {
                    til_msg.setHelperText("Vui l??ng kh??ng b??? tr???ng tr?????ng!!!");
                } else {
                    til_msg.setHelperText(null);
                }
            }
        });
        btn_send.setOnClickListener(v -> {
            String msg = Objects.requireNonNull(til_msg.getEditText()).getText().toString();
            if (TextUtils.isEmpty(msg)) {
                til_msg.setHelperText("Vui l??ng kh??ng b??? tr???ng tr?????ng!!!");
            } else {
                til_msg.setHelperText(null);
                ViewUtils.progressBarProcess(true, rl_pbar, activity);
                feedbackDAO.postFeedBack(msg, is -> {
                    if (is) {
                        Toast.makeText(activity, "C???m ??n b???n ???? ????ng g??p ?? ki???n.", Toast.LENGTH_LONG).show();
                        ad.dismiss();
                    }
                    ViewUtils.progressBarProcess(false, rl_pbar, activity);

                });
            }
        });

        ad.show();
        ad.getWindow().setBackgroundDrawableResource(R.color.transparent);
    }

    private void showDialogChangePassword(MenuItem item) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
        @SuppressLint("InflateParams")
        View view = activity.getLayoutInflater().inflate(R.layout.layout_change_password, null);
        TextInputLayout til_current_pass = view.findViewById(R.id.til_current_pass);
        TextInputLayout til_new_pass = view.findViewById(R.id.til_new_pass);
        TextView tv_err = view.findViewById(R.id.tv_err);
        RelativeLayout rl_pbar = view.findViewById(R.id.rl_pbar);
        Button btn_confirm = view.findViewById(R.id.btn_confirm);
        Button btn_cancel = view.findViewById(R.id.btn_cancel);
        alertDialog.setView(view);
        final AlertDialog ad = alertDialog.create();
        ad.setCancelable(false);

        // Listeners
        if (!userDAO.isForgotPasswordFlag()) {
            btn_cancel.setOnClickListener(v -> {
                ad.dismiss();
                if (item != null)
                    item.setChecked(false);
            });
        } else {
            btn_cancel.setVisibility(View.GONE);
            Toast.makeText(activity, "B???n v???a y??u c???u c???p l???i m???t kh???u\n????? ?????m b???o an to??n, b???n h??y ?????i m???t kh???u tr?????c khi s??? d???ng ???ng d???ng.", Toast.LENGTH_LONG).show();
        }
        btn_confirm.setOnClickListener(v -> {
            // Ki???m tra d??? li???u h???p l???
            String currentPass = Objects.requireNonNull(til_current_pass.getEditText()).getText().toString();
            if (TextUtils.isEmpty(currentPass)) {
                tv_err.setVisibility(View.VISIBLE);
                tv_err.setText(R.string.nhap_day_du_du_lieu);
                return;
            }
            String newPass = Objects.requireNonNull(til_new_pass.getEditText()).getText().toString();
            if (TextUtils.isEmpty(newPass)) {
                tv_err.setVisibility(View.VISIBLE);
                tv_err.setText(R.string.nhap_day_du_du_lieu);
            } else if (newPass.length() < 6) {
                tv_err.setVisibility(View.VISIBLE);
                tv_err.setText(R.string.mat_khau_6_ky_tu);
            } else if (newPass.equals(currentPass)) {
                tv_err.setVisibility(View.VISIBLE);
                tv_err.setText(R.string.trung_mat_khau);
            } else {
                tv_err.setVisibility(View.GONE);
                ViewUtils.progressBarProcess(true, rl_pbar, activity);
                userDAO.changePassword(currentPass, newPass, new UserDAO.IControlData() {
                    @Override
                    public void isSuccess(boolean is) {
                        ViewUtils.progressBarProcess(false, rl_pbar, activity);
                        if (is) {
                            if (userDAO.isForgotPasswordFlag()) {
                                userDAO.setForgotPasswordFlag();
                            }
                            Toast.makeText(activity, "C???p nh???t m???t kh???u th??nh c??ng", Toast.LENGTH_SHORT).show();
                            ad.dismiss();
                            if (item != null)
                                item.setChecked(false);
                        } else {
                            tv_err.setVisibility(View.VISIBLE);
                            tv_err.setText(R.string.mat_khau_hien_tai_sai);
                        }
                    }
                });
            }


        });
        ad.show();
        ad.getWindow().setBackgroundDrawableResource(R.color.transparent);
    }

    // Data ASync
    private boolean cancel;

    private int currentPriority;

    private Button btn_re_call;
    private TextView tv_status;
    private final String status_calling = "??ang g???i";
    private final String status_end_call = "K???t th??c cu???c g???i";
    private String code;

    private TextView tv_name_call, tv_phone_call, tv_time_call;
    private CircularProgressIndicator cpi_call;
    private ImageView iv_priority_call;

    private void showDialogCalling(List<Guardian> guardianList) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
        @SuppressLint("InflateParams")
        View view = activity.getLayoutInflater().inflate(R.layout.layout_calling_guardian, null);
        tv_name_call = view.findViewById(R.id.tv_name);
        tv_phone_call = view.findViewById(R.id.tv_phone);
        tv_time_call = view.findViewById(R.id.tv_time);
        cpi_call = view.findViewById(R.id.cpi);
        iv_priority_call = view.findViewById(R.id.iv_priority);

        btn_re_call = view.findViewById(R.id.btn_re_call);
        btn_re_call.setOnClickListener(v -> callProcess(guardianList, cpi_call, tv_time_call, tv_name_call, tv_phone_call, iv_priority_call));

        tv_status = view.findViewById(R.id.tv_status);

        alertDialog.setView(view);
        final AlertDialog ad = alertDialog.create();

        view.findViewById(R.id.btn_close).setOnClickListener(v -> {
            Snackbar snackbar = Snackbar.make(v, "X??c nh???n ????ng", Snackbar.LENGTH_LONG);
            snackbar.setAction("?????ng ??", v1 -> {
                if (!cancel)
                    listenersCallbacksDAO.setStatus(1);
                snackbar.dismiss();
                ad.cancel();
                cancel = true;
            });
            snackbar.setActionTextColor(getColor(R.color.yellow));
            snackbar.show();
        });
        ad.setCancelable(false);
        callProcess(guardianList, cpi_call, tv_time_call, tv_name_call, tv_phone_call, iv_priority_call);
        ad.show();

    }

    private void callProcess(List<Guardian> guardianList, CircularProgressIndicator cpi,
                             TextView tv_time, TextView tv_name, TextView tv_phone, ImageView iv_priority) {
        // Reset data
        currentPriority = guardianList.size();
        cancel = false;
        btn_re_call.setVisibility(View.GONE);
        tv_status.setText(status_calling);
        code = UtilsClazz.random8Code();
        setListenerCallbacks(code);
        // Call
        for (Guardian guardian : guardianList) {
            new MyAsyncTask(cpi, tv_time, tv_name, tv_phone, iv_priority, guardian).execute();
        }
    }

    private void setListenerCallbacks(String code) {
        listenersCallbacksDAO.createListenersCallbacks(code, new ListenersCallbacksDAO.IControlData() {
            @Override
            public void isOk(boolean is) {
                if (is) {
                    listenersCallbacksDAO.checkStatusOnChange(code, new ListenersCallbacksDAO.IControlData() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void sendStatus(int status) {
                            if (status != ListenersCallbacksDAO.STATUS_WAITING) {
                                Toast.makeText(activity, "Gi??m h??? ???? nh???c m??y", Toast.LENGTH_LONG).show();
                                tv_status.setText("G???i th??nh c??ng");
                                listenersCallbacksDAO.setStatus(1);
                                listenersCallbacksDAO.removeListener();
                                cancel = true;
                            }
                        }
                    });
                }
            }
        });

    }

    private void initDataSession() {
        tv_name.setText(currentUser.getName());
        tv_phone.setText(currentUser.getUid());
        View headerView = navigationView.getHeaderView(0);
        TextView tvn = headerView.findViewById(R.id.tv_name2);
        TextView tvp = headerView.findViewById(R.id.tv_phone2);
        tvn.setText(currentUser.getName());
        tvp.setText(currentUser.getUid());
    }

    @SuppressLint("StaticFieldLeak")
    private class MyAsyncTask extends AsyncTask<Void, Integer, Void> {
        private final int red = getColor(R.color.red);
        private final int teal = getColor(R.color.teal_700);
        private final int yellow = getColor(R.color.yellow);

        private final CircularProgressIndicator cpi;
        private final TextView tv_time;
        private final ImageView iv_priority;
        private final TextView tv_name;
        private final TextView tv_phone;

        private final Guardian guardian;
        private int setColor;
        private boolean added;

        private MyAsyncTask(CircularProgressIndicator cpi, TextView tv_time, TextView tv_name, TextView tv_phone, ImageView iv_priority, Guardian guardian) {
            this.cpi = cpi;
            this.tv_time = tv_time;
            this.iv_priority = iv_priority;
            this.tv_name = tv_name;
            this.tv_phone = tv_phone;
            this.guardian = guardian;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            cpi.setIndicatorColor(getColor(R.color.gray_light));

        }

        @Override
        protected void onCancelled(Void aVoid) {
            super.onCancelled(aVoid);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            for (int i = 0; i <= GuardianDAO.TIME_LIMIT; i++) {
                if (isCancelled())
                    break;
                if (cancel) {
                    this.cancel(true);
                    break;
                }
                publishProgress(i);
                SystemClock.sleep(1000);
            }
            return null;
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            int value = values[0];
            if (value == 0) {
                cpi.setIndicatorColor(teal);
                tv_time.setTextColor(teal);
            } else if (value > 7 && value < 13) {
                if (setColor == 0) {
                    setColor++;
                    cpi.setIndicatorColor(yellow);
                    tv_time.setTextColor(yellow);
                }
            } else if (value == 13) {
                cpi.setIndicatorColor(red);
                tv_time.setTextColor(red);
            }
            cpi.setProgressCompat(value, true);
            tv_time.setText(GuardianDAO.TIME_LIMIT - value + "s");
            if (!added) {
                added = true;
//                 G???i th??ng b??o kh???n c???p/ g???i ??i???n
                DataCallGuardian dataCallGuardian = new DataCallGuardian(code, null, guardian.getUid(), null);
                LatLng location = locationDAO.getCurrentLocation();
                dataCallGuardian.setLatitude(location.latitude);
                dataCallGuardian.setLongitude(location.longitude);
                guardianDAO.sendNotification(dataCallGuardian, null);

                tv_name.setText(guardian.getName());
                tv_phone.setText(guardian.getUid());

                if (guardian.getPriority() == 1) {
                    iv_priority.setImageResource(R.drawable.ic_first);
                } else if (guardian.getPriority() == 2) {
                    iv_priority.setImageResource(R.drawable.ic_second);
                } else if (guardian.getPriority() == 3) {
                    iv_priority.setImageResource(R.drawable.ic_third);
                } else {
                    iv_priority.setImageDrawable(null);
                }
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            // ???? g???i t???i ng?????i cu???i c??ng
            if (guardian.getPriority() == currentPriority) {
                Log.e("END",currentUser.getGuardians().toString());
                callProcess(currentUser.getGuardians(), cpi_call, tv_time_call, tv_name_call, tv_phone_call, iv_priority_call);
            }
        }
    }
}