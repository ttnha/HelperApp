package com.app.helper.Views.Adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.app.helper.Followed.DAO.FollowedListDAO;
import com.app.helper.Followed.Model.Followed;
import com.app.helper.Guardian.DAO.GuardianDAO;
import com.app.helper.Guardian.Model.Guardian;
import com.app.helper.Notification.Model.DataAddGuardian;
import com.app.helper.R;
import com.app.helper.User.DAO.ListenersCallbacksDAO;
import com.app.helper.User.Model.Users;
import com.app.helper.Utils.UtilsClazz;
import com.app.helper.Utils.ViewUtils;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class RV_GuardiansAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<Guardian> guardianList;
    private final Activity activity;
    private final GuardianDAO guardianDAO;
    private final FollowedListDAO followedListDAO;

    private final Users currentUser;
    // View in parent
    private final Button btn_confirm;
    private final LinearLayout ln_pbar;

    private int counter = 59;

    public RV_GuardiansAdapter(Activity activity) {
        this.activity = activity;
        this.btn_confirm = activity.findViewById(R.id.btn_confirm);
        this.ln_pbar = activity.findViewById(R.id.ln_pbar);

        this.followedListDAO = new FollowedListDAO();
        this.guardianList = new ArrayList<>();
        this.guardianDAO = new GuardianDAO();
        this.currentUser = guardianDAO.getCurrentUser();

        // Cập nhật danh sách sắp xếp
        this.btn_confirm.setOnClickListener(v -> {
            ViewUtils.progressBarProcess(true, ln_pbar, activity);
            guardianDAO.setGuardians(currentUser.getUid(), guardianList, new GuardianDAO.IControlData() {
                @Override
                public void isOK(boolean is) {
                    if (is) {
                        Toast.makeText(activity, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                        btn_confirm.setVisibility(View.GONE);
                    } else {
                        Toast.makeText(activity, "Cập nhật thất bại!", Toast.LENGTH_SHORT).show();
                    }
                    ViewUtils.progressBarProcess(false, ln_pbar, activity);
                }
            });
        });

    }

    public void setGuardianList(List<Guardian> guardians) {
        this.guardianList.clear();
        this.guardianList.addAll(guardians);
        // Thêm thằng cuối cùng tượng trưng cho item "THÊM..."
        this.guardianList.add(new Guardian());
        this.notifyDataSetChanged();
    }

    public void setItemTouchHelper(RecyclerView rv_guardians) {
        new ItemTouchHelper(simpleCallback).attachToRecyclerView(rv_guardians);
    }

    private final ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP |
                    ItemTouchHelper.DOWN, 0
    ) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            int from = viewHolder.getAdapterPosition();
            if (from != guardianList.size() - 1) {
                notifyItemChanged(from);
                int to = target.getAdapterPosition();
                if (to < guardianList.size() - 1) {
                    notifyItemChanged(to);
                    Guardian gFrom = guardianList.get(from);
                    Guardian gTo = guardianList.get(to);
                    int priorityFrom = gFrom.getPriority();
                    int priorityTo = gTo.getPriority();
                    gFrom.setPriority(priorityTo);
                    gTo.setPriority(priorityFrom);
                    Collections.swap(guardianList, from, to);
                    notifyItemMoved(from, to);
                    btn_confirm.setVisibility(View.VISIBLE);
                }
            }

            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

        }
    };


//    private void deleteItem(int index) {
//        // Đồng thời cập nhật lại độ ưu tiên cho các item sau index này --1
//        for (int i = index; i < this.guardianList.size() - 1; i++) {
//            Guardian guardian = this.guardianList.get(i);
//            guardian.setPriority(guardian.getPriority() - 1);
//            this.notifyItemChanged(i);
//        }
//        this.guardianList.remove(index - 1);
//        this.notifyItemRemoved(index - 1);
//    }

    private void insertItem(Guardian guardian) {
        this.guardianList.add(guardian.getPriority() - 1, guardian);
        this.notifyItemInserted(guardian.getPriority() - 1);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // viewType = 1 là item bình thường, else là item "THÊM ..."
        if (viewType == 1) {
            return new GuardiansHolder(LayoutInflater.from(activity).inflate(R.layout.rv_item_guardians, parent, false));
        } else {
            return new GuardiansLastHolder(LayoutInflater.from(activity).inflate(R.layout.rv_item_last_guardian, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == 1) {
            Guardian guardian = guardianList.get(position);
            GuardiansHolder guardiansHolder = (GuardiansHolder) holder;
            ImageView iv_rank = guardiansHolder.iv_rank;
            if (position == 0) {
                iv_rank.setImageResource(R.drawable.ic_first);
            } else if (position == 1) {
                iv_rank.setImageResource(R.drawable.ic_second);
            } else if (position == 2) {
                iv_rank.setImageResource(R.drawable.ic_third);
            } else {
                iv_rank.setImageDrawable(null);
            }
            guardiansHolder.tv_name.setText(guardian.getName());
            guardiansHolder.tv_phone.setText(guardian.getUid());

        } else {
            GuardiansLastHolder guardiansLastHolder = (GuardiansLastHolder) holder;
            guardiansLastHolder.ll_add.setOnClickListener(v -> showDialogAdd());

        }
    }

    @Override
    public int getItemViewType(int position) {
        return position == this.guardianList.size() - 1 ? 0 : 1;
    }

    @Override
    public int getItemCount() {
        return guardianList.size();
    }

    // Không cho phép thêm người đã tồn tại
    private boolean isExistsPhoneNumber(String phone) {
        for (int i = 0; i < this.guardianList.size() - 1; i++) {
            if (this.guardianList.get(i).getUid().equals(phone)) return true;
        }
        return false;
    }

    private final String TXT_NOT_EXISTS = "Số điện thoại không tồn tại";
    private final String TXT_IS_EXISTS = "Người này hiện đang giám hộ bạn";
    private final String TXT_ME = "Không thể thêm chính bạn";

    private void showDialogAdd() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
        @SuppressLint("InflateParams")
        View view = activity.getLayoutInflater().inflate(R.layout.layout_add_members, null);
        TextInputLayout et_phone = view.findViewById(R.id.et_phone);
        TextView tv = view.findViewById(R.id.tv);
        tv.setText(activity.getString(R.string.them_nguoi_than));
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
            tv_warning.setVisibility(View.INVISIBLE);
            tv_warning.setText(TXT_NOT_EXISTS);
            ViewUtils.hideSoftKeyboard(et_phone);
            ViewUtils.progressBarProcess(true, rl_pbar, activity);
            // Kiểm tra uid này có tồn tại hay không, có thì thêm, else
            guardianDAO.getGuardianByUid(phone, new GuardianDAO.IControlData() {
                @Override
                public void getUser(Users users) {
                    if (users != null) {
                        // Tồn tại user
                        // Bắng notification cho thằng này
                        String code = UtilsClazz.random6Code();
                        DataAddGuardian dataAddGuardian = new DataAddGuardian(null, null, code, phone, null);
                        guardianDAO.sendNotification(dataAddGuardian, new GuardianDAO.IControlData() {
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
                                                            Followed followed = new Followed();
                                                            followed.setUid(currentUser.getUid());
                                                            followed.setName(currentUser.getName());
                                                            followedListDAO.putFollowedAfterAddGuardian(phone
                                                                    , followed, new FollowedListDAO.IControlData() {
                                                                        @Override
                                                                        public void isOK(boolean is) {
                                                                            if (is) {
                                                                                Guardian guardian = new Guardian();
                                                                                guardian.setName(users.getName());
                                                                                guardian.setUid(phone);
                                                                                // Trừ thằng item cuối cùng
                                                                                guardian.setPriority(guardianList.size());
                                                                                insertItem(guardian);
                                                                                guardianDAO.setGuardians(currentUser.getUid(), guardianList, new GuardianDAO.IControlData() {
                                                                                    @Override
                                                                                    public void isOK(boolean is) {
                                                                                        Toast.makeText(activity, "Thêm thành công", Toast.LENGTH_LONG).show();
                                                                                    }
                                                                                });
                                                                            } else {
                                                                                Toast.makeText(activity, "Đã có lỗi xảy ra", Toast.LENGTH_LONG).show();
                                                                            }
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

    protected static class GuardiansHolder extends RecyclerView.ViewHolder {
        private final TextView tv_name;
        private final TextView tv_phone;
        private final ImageView iv_rank;

        public GuardiansHolder(@NonNull View itemView) {
            super(itemView);
            tv_name = itemView.findViewById(R.id.tv_name);
            tv_phone = itemView.findViewById(R.id.tv_phone);
            iv_rank = itemView.findViewById(R.id.iv_rank);
        }
    }

    protected static class GuardiansLastHolder extends RecyclerView.ViewHolder {
        private final LinearLayout ll_add;

        public GuardiansLastHolder(@NonNull View itemView) {
            super(itemView);
            ll_add = itemView.findViewById(R.id.ll_add);
        }
    }
}
