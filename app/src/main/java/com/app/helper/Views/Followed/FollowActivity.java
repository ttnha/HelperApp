package com.app.helper.Views.Followed;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.app.helper.Followed.DAO.FollowedListDAO;
import com.app.helper.Followed.Model.Followed;
import com.app.helper.Guardian.DAO.GuardianDAO;
import com.app.helper.Guardian.Model.Guardian;
import com.app.helper.Notification.Model.DataAddGuardian;
import com.app.helper.R;
import com.app.helper.User.DAO.ListenersCallbacksDAO;
import com.app.helper.User.DAO.UserDAO;
import com.app.helper.User.Model.Users;
import com.app.helper.Utils.UtilsClazz;
import com.app.helper.Utils.ViewUtils;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FollowActivity extends AppCompatActivity {
    public static final String KEY_FOLLOWED = "KEY_FOLLOWED";
    public static final String KEY_FOLLOWED_LIST = "KEY_FOLLOWED_LIST";
    private final Activity activity = this;

    private TextView tv_name, tv_phone, tv_empty_list;
    private RelativeLayout rl_pbar;
    private RadioGroup rd_g;
    private LinearLayout ll_bt_s, ll_map_all, ln_title;
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private RelativeLayout rl_add_history, rl_add_fav;

    private Users currentUser;
    private FollowedListDAO followedListDAO;
    private Followed followed_current;
    private List<Followed> followedList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Model
        currentUser = UserDAO.getUsersSession();
        if (currentUser == null) finish();
        setContentView(R.layout.activity_follow);
        followedListDAO = new FollowedListDAO();
        // View
        getWidgets();
        initBottomSheet();
        initFollowedList();
        initDataSession();
        setListeners();
    }

    private RadioButton rd_current;

    private void initBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(ll_bt_s);
        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED || newState == BottomSheetBehavior.STATE_HIDDEN) {
                    if (rd_current != null) {
                        rd_current.setChecked(false);
                        followed_current = null;
                    }
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
    }

    private boolean isLong;

    @SuppressLint("SetTextI18n")
    private void initFollowedList() {
        rl_pbar.setVisibility(View.VISIBLE);
        followedListDAO.getFollowedList(currentUser.getUid(), new FollowedListDAO.IControlData() {
            @Override
            public void dataFollowed(List<Followed> followedList) {
                rl_pbar.setVisibility(View.GONE);
                ViewGroup.LayoutParams layoutParams = new ViewGroup
                        .LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                if (followedList == null) {
                    tv_empty_list.setVisibility(View.VISIBLE);
                    FollowActivity.this.followedList = new ArrayList<>();
                } else {
                    FollowActivity.this.followedList = followedList;
                    ln_title.setVisibility(View.VISIBLE);
                    for (int i = 0; i < followedList.size(); i++) {
                        Followed followed = followedList.get(i);
                        @SuppressLint("InflateParams")
                        RadioButton radioButton = (RadioButton) getLayoutInflater().inflate(R.layout.layout_item_followed, null);
                        radioButton.setId(i);
                        radioButton.setText(followed.getName() + " - " + followed.getUid());

                        radioButton.setOnClickListener(v -> {
                            if (isLong) return;
                            radioButton.setChecked(true);
                            rd_current = radioButton;
                            followed_current = followed;
                            if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED || bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
                                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                            }
                        });

                        radioButton.setOnLongClickListener(v -> {
                            isLong = true;
                            rd_current = radioButton;
                            showDialogDelete(followed);
                            return false;
                        });

                        radioButton.setLayoutParams(layoutParams);
                        rd_g.addView(radioButton);
                    }
                }
                // add Viewpp
                @SuppressLint("InflateParams")
                RadioButton radioButton = (RadioButton) getLayoutInflater().inflate(R.layout.layout_item_followed_add, null);
                int id = 999;
                radioButton.setId(id);
                radioButton.setOnClickListener(v ->
                        showDialogAdd()
                );
                radioButton.setLayoutParams(layoutParams);
                rd_g.addView(radioButton);
            }
        });
    }

    private final String TXT_NOT_EXISTS = "Số điện thoại không tồn tại";
    private final String TXT_IS_EXISTS = "Người này hiện đang giám hộ bạn";
    private final String TXT_ME = "Không thể thêm chính bạn";
    private final String TXT_IS_GUARDIAN = "Không thể thêm giám hộ vào danh sách thành viên";
    private int counter = 59;

    private void showDialogAdd() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
        @SuppressLint("InflateParams")
        View view = activity.getLayoutInflater().inflate(R.layout.layout_add_members, null);
        TextInputLayout et_phone = view.findViewById(R.id.et_phone);
        TextView tv = view.findViewById(R.id.tv);
        tv.setText(activity.getString(R.string.them_thanh_vien));
        TextView tv_warning = view.findViewById(R.id.tv_warning);
        TextView tv_ok = view.findViewById(R.id.tv_ok);
        RelativeLayout rl_pbar = view.findViewById(R.id.rl_pbar);
        alertDialog.setView(view);
        final AlertDialog ad = alertDialog.create();

        // Set Listeners
        Objects.requireNonNull(et_phone.getEditText()).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s.toString()))
                    ViewUtils.showSoftKeyboard(et_phone);
                tv_warning.setVisibility(View.INVISIBLE);
                tv_ok.setEnabled(!TextUtils.isEmpty(s.toString()));
            }
        });
        tv_ok.setOnClickListener(v -> {
            String phone = Objects.requireNonNull(et_phone.getEditText()).getText().toString();
            if (isExistsPhoneNumber(phone)) {
                tv_warning.setText(TXT_IS_EXISTS);
                tv_warning.setVisibility(View.VISIBLE);
                return;
            }
            if (currentUser.getUid().equals(phone)) {
                tv_warning.setText(TXT_ME);
                tv_warning.setVisibility(View.VISIBLE);
                return;
            }
            if (followedListDAO.isGuardian(phone,currentUser.getGuardians())) {
                tv_warning.setText(TXT_IS_GUARDIAN);
                tv_warning.setVisibility(View.VISIBLE);
                return;
            }
            tv_warning.setVisibility(View.INVISIBLE);
            tv_warning.setText(TXT_NOT_EXISTS);
            ViewUtils.hideSoftKeyboard(et_phone);
            ViewUtils.progressBarProcess(true, rl_pbar, activity);
            // Kiểm tra uid này có tồn tại hay không, có thì thêm, else
            followedListDAO.getFollowedByUid(phone, new FollowedListDAO.IControlData() {
                @Override
                public void getUser(Users users) {
                    if (users != null) {
                        // Tồn tại user
                        // Bắng notification cho thằng này
                        String code = UtilsClazz.random6Code();
                        DataAddGuardian dataAddGuardian = new DataAddGuardian(null, null, code, phone, null);
                        followedListDAO.sendNotification(dataAddGuardian, currentUser, new FollowedListDAO.IControlData() {
                            @Override
                            public void isOK(boolean is) {
                                ad.setCancelable(false);
                                ListenersCallbacksDAO listenersCallbacksDAO = new ListenersCallbacksDAO();
                                Toast.makeText(activity, "Mã liên kết có hiệu lực 60 giây!!!", Toast.LENGTH_SHORT).show();
                                // Đăng ký sự kiện callback cho 2 bên
                                listenersCallbacksDAO.createListenersCallbacks(code, new ListenersCallbacksDAO.IControlData() {
                                    @Override
                                    public void isOk(boolean is) {
                                        if (is) {
                                            listenersCallbacksDAO.checkStatusOnChange(code, new ListenersCallbacksDAO.IControlData() {
                                                @Override
                                                public void sendStatus(int status) {
                                                    if (status > ListenersCallbacksDAO.STATUS_WAITING) {
                                                        if (status == ListenersCallbacksDAO.STATUS_ACCEPT) {
                                                            Log.e("***MSG", "2");
                                                            Guardian guardian = new Guardian();
                                                            guardian.setUid(currentUser.getUid());
                                                            guardian.setName(currentUser.getName());
                                                            new GuardianDAO().setGuardians(phone, guardian, new GuardianDAO.IControlData() {
                                                                @Override
                                                                public void getUser(Users users) {
                                                                    Followed followed = new Followed();
                                                                    followed.setUid(users.getUid());
                                                                    followed.setName(users.getName());
                                                                    followedList.add(followed);
                                                                    Log.e("***MSG", "3");
                                                                    followedListDAO.putFollowedAfterAddGuardian(currentUser.getUid()
                                                                            , followed, new FollowedListDAO.IControlData() {
                                                                                @SuppressLint("SetTextI18n")
                                                                                @Override
                                                                                public void isOK(boolean is) {
                                                                                    if (is) {
                                                                                        @SuppressLint("InflateParams")
                                                                                        RadioButton radioButton = (RadioButton) getLayoutInflater().inflate(R.layout.layout_item_followed, null);
                                                                                        radioButton.setText(followed.getName() + " - " + followed.getUid());
                                                                                        radioButton.setId(followedList.size() - 1);
                                                                                        radioButton.setOnClickListener(v ->
                                                                                                showDialogAdd()
                                                                                        );
                                                                                        radioButton.setLayoutParams(new ViewGroup
                                                                                                .LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                                                                                        rd_g.addView(radioButton, followedList.size()-1);
                                                                                        Toast.makeText(activity, "Thêm thành công", Toast.LENGTH_LONG).show();
                                                                                    } else {
                                                                                        Toast.makeText(activity, "Đã có lỗi xảy ra", Toast.LENGTH_LONG).show();
                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            });
                                                        } else if (status == ListenersCallbacksDAO.STATUS_CANCEL) {
                                                            Toast.makeText(activity, "Bị từ chối", Toast.LENGTH_LONG).show();
                                                        }
                                                        listenersCallbacksDAO.removeListener();
                                                        counter = 0;
                                                        ad.cancel();
                                                        ViewUtils.progressBarProcess(false, rl_pbar, activity);
                                                    }
                                                }
                                            });

                                            Snackbar snackbar = Snackbar.make(v, "Mã liên kết: " + code, Snackbar.LENGTH_INDEFINITE);
                                            snackbar.setActionTextColor(activity.getColor(R.color.yellow));
                                            snackbar.setAction("HỦY", v1 -> {
                                                snackbar.dismiss();
                                                counter = 0;
                                                if (ad.isShowing()) {
                                                    ViewUtils.progressBarProcess(false, rl_pbar, activity);
                                                    Toast.makeText(activity, "Thêm thất bại", Toast.LENGTH_SHORT).show();
                                                    ad.cancel();
                                                    listenersCallbacksDAO.setStatus(1);
                                                    listenersCallbacksDAO.removeListener();
                                                }
                                            });
                                            snackbar.show();
                                        }
                                    }
                                });

                                AsyncTask.execute(() -> {
                                    while (counter > 0) {
                                        counter--;
                                        SystemClock.sleep(1000);
                                        if (counter == 0) {
                                            break;
                                        }
                                    }
                                });
                                AsyncTask.execute(() -> {
                                    while (counter != 0) {
                                        SystemClock.sleep(1000);
                                    }
                                    counter = 59;
                                    if (ad.isShowing()) {
                                        ad.setOnCancelListener(dialog -> {
                                                    ViewUtils.progressBarProcess(false, rl_pbar, activity);
                                                    Toast.makeText(activity, "Mã liên kết đã hết hiệu lực!!!", Toast.LENGTH_SHORT).show();
                                                    listenersCallbacksDAO.removeListener();
                                                }
                                        );
                                        ad.cancel();
                                    }
                                });
                            }
                        });
                    } else {
                        tv_warning.setVisibility(View.VISIBLE);
                        ViewUtils.progressBarProcess(false, rl_pbar, activity);
                    }
                }
            });

        });
        view.findViewById(R.id.tv_cancel).setOnClickListener(v -> ad.cancel());
        ad.show();

    }

    private boolean isExistsPhoneNumber(String phone) {
        for (int i = 0; i < this.followedList.size() - 1; i++) {
            if (this.followedList.get(i).getUid().equals(phone)) return true;
        }
        return false;
    }

    private void showDialogDelete(Followed followed) {
        String message = followed.getName() + "\n" + followed.getUid();
        ViewUtils.showDialogConfirm("Xác nhận xóa thành viên", message, activity, is -> {
            if (is) {
                // Xử lý xóa thành viên
                followedListDAO.deleteFollowed(currentUser.getUid(), followed.getUid(), new FollowedListDAO.IControlData() {
                    @Override
                    public void isOK(boolean is) {
                        if (is) {
                            runOnUiThread(() -> rd_g.removeView(rd_current));
                            followedList.remove(followed);
                            Toast.makeText(activity, "Xóa thành công", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
            isLong = false;
        });
    }

    private void getWidgets() {
        tv_name = findViewById(R.id.tv_name);
        tv_phone = findViewById(R.id.tv_phone);
        tv_empty_list = findViewById(R.id.tv_empty_list);

        rd_g = findViewById(R.id.rd_g);

        ll_bt_s = findViewById(R.id.ll_bt_s);
        ll_map_all = findViewById(R.id.ll_map_all);
        ln_title = findViewById(R.id.ln_title);

        rl_pbar = findViewById(R.id.rl_pbar);
        rl_add_fav = findViewById(R.id.rl_add_fav);
        rl_add_history = findViewById(R.id.rl_add_history);
    }

    private void setListeners() {
        @SuppressLint("NonConstantResourceId") View.OnClickListener onClickListener = v -> {
            if (followed_current == null) return;
            Intent intent;
            if (v.getId() == R.id.rl_add_fav) {
                intent = new Intent(this, FamiliarPlaceActivity.class);
            } else {// Location History
                intent = new Intent(this, HistoryMoveActivity.class);
            }
            intent.putExtra(KEY_FOLLOWED, followed_current);
            startActivity(intent);
        };
        rl_add_fav.setOnClickListener(onClickListener);
        rl_add_history.setOnClickListener(onClickListener);

        ll_map_all.setOnClickListener(v -> {
            if (this.followedList == null) {
                Toast.makeText(activity, "Danh sách theo dõi trống", Toast.LENGTH_LONG).show();
                return;
            }
            Intent intent = new Intent(activity, GlobalMapActivity.class);
            intent.putExtra(KEY_FOLLOWED_LIST, new Gson().toJson(this.followedList));
            startActivity(intent);
        });
    }

    private void initDataSession() {
        tv_name.setText(currentUser.getName());
        tv_phone.setText(currentUser.getUid());
    }

    // Binding view
    public void goHome(View view) {
        finish();
    }

    public void onHideBottomSheet(View view) {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }
}