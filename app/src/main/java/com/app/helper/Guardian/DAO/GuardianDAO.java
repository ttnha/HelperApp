package com.app.helper.Guardian.DAO;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import com.app.helper.Firebases.FireBaseInit;
import com.app.helper.Firebases.TableName;
import com.app.helper.Guardian.Model.Guardian;
import com.app.helper.Notification.Model.BaseData;
import com.app.helper.Notification.Model.DataAddGuardian;
import com.app.helper.Notification.Model.DataCallGuardian;
import com.app.helper.Notification.Model.NotificationModel;
import com.app.helper.Notification.Service.INotificationService;
import com.app.helper.Notification.Service.NotificationService;
import com.app.helper.User.DAO.UserDAO;
import com.app.helper.User.Model.Users;
import com.google.firebase.database.DatabaseReference;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GuardianDAO {
    public static final int TIME_LIMIT = 45;
    private static final String TABLE_NAME = TableName.Users.name();
    private static final String CHILD_NAME = "guardians";

    public List<Guardian> getGuardianList() {
        Users users = getCurrentUser();
        if (users != null) {
            return users.getGuardians() != null ? users.getGuardians() : new ArrayList<>();
        }
        return null;
    }

    public void getGuardianByUid(String uid, IControlData iControlData) {
        FireBaseInit.getInstance().getReference()
                .child(TABLE_NAME)
                .child(uid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult() != null) {
                            iControlData.getUser(task.getResult().getValue(Users.class));
                            return;
                        }
                    }
                    iControlData.getUser(null);
                });
    }

    public void setGuardians(String uid, List<Guardian> guardians, IControlData iControlData) {
        // Xóa thằng cuối cùng, nó là thằng item "THÊM..."
        List<Guardian> guardiansTmp = new ArrayList<>(guardians);
        guardiansTmp.remove(guardians.size() - 1);
        FireBaseInit.getInstance().getReference()
                .child(TABLE_NAME)
                .child(uid)
                .child(CHILD_NAME)
                .setValue(guardiansTmp)
                .addOnCompleteListener(task -> iControlData.isOK(task.isSuccessful()));
    }

    public void setGuardians(String uid, Guardian guardian, IControlData iControlData) {
        // Lấy ra danh sách người thân hiện tại
        DatabaseReference mRef = FireBaseInit.getInstance().getReference()
                .child(TABLE_NAME)
                .child(uid);
        mRef.get().addOnCompleteListener(task -> {
            if (task.getResult().exists()) {
                Users userDB = task.getResult().getValue(Users.class);
                if (userDB != null) {
                    List<Guardian> guardianList = userDB.getGuardians();
                    int currentIndex = 1;
                    if (guardianList != null) {
                        // Thêm người này vào cuối danh sách người thân
                        currentIndex = guardianList.size() + 1;
                    } else {
                        guardianList = new ArrayList<>();
                    }
                    guardian.setPriority(currentIndex);
                    guardianList.add(guardian);
                    mRef.child("guardians").setValue(guardianList)
                            .addOnCompleteListener(task1 -> iControlData.getUser(userDB));
                }


            }
        });
    }

    public Users getCurrentUser() {
        return UserDAO.getUsersSession();
    }

    // Gửi thông báo cho user được thêm làm giám hộ
    public void sendNotification(BaseData data, IControlData iControlData) {
        Users currentUser = getCurrentUser();
        FireBaseInit.getInstance().getReference()
                .child(TABLE_NAME)
                .child(data.getUid()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult() != null) {
                    Users users = task.getResult().getValue(Users.class);
                    if (users != null) {
                        String token = users.getToken();
                        if (data.getType() == NotificationService.TYPE_ADD_GUARDIAN) {
                            DataAddGuardian dataAddGuardian = (DataAddGuardian) data;
                            dataAddGuardian.setName(users.getName());
                            dataAddGuardian.setContent(currentUser.getUid() + " muốn liên kết tới bạn");
                            processSendNotification(token, dataAddGuardian, iControlData);
                        } else if (data.getType() == NotificationService.TYPE_CALL_GUARDIAN) {
                            DataCallGuardian dataCallGuardian = (DataCallGuardian) data;
                            dataCallGuardian.setName(currentUser.getName());
                            processSendNotification(token, dataCallGuardian, null);
                        }
                    }

                }
            }
        });

    }


    @SuppressLint("MissingPermission")
    private void processSendNotification(String token, BaseData data, IControlData iControlData) {
        NotificationModel notificationModel = new NotificationModel();
        notificationModel.setTo(token);

        // Send thông báo muốn thêm giám hộ
        if (data.getType() == NotificationService.TYPE_ADD_GUARDIAN) {
            DataAddGuardian dataAddGuardian = (DataAddGuardian) data;
            dataAddGuardian.setTime_limit(LocalDateTime.now().plusMinutes(1).toString());
            notificationModel.setData(dataAddGuardian);
            INotificationService.INSTANCE_SINGLE.sendNotification(notificationModel)
                    .enqueue(new Callback<NotificationModel>() {
                        @Override
                        public void onResponse(@NonNull Call<NotificationModel> call, @NonNull Response<NotificationModel> response) {
                            iControlData.isOK(response.isSuccessful());
                        }

                        @Override
                        public void onFailure(@NonNull Call<NotificationModel> call, @NonNull Throwable t) {
                            iControlData.isOK(false);
                        }
                    });
        } else if (data.getType() == NotificationService.TYPE_CALL_GUARDIAN) {
            DataCallGuardian dataCallGuardian = (DataCallGuardian) data;
            dataCallGuardian.setUid(getCurrentUser().getUid());
            dataCallGuardian.setTime_limit(LocalDateTime.now().plusSeconds(TIME_LIMIT).toString());
            notificationModel.setData(dataCallGuardian);
            INotificationService.INSTANCE_SINGLE.sendNotification(notificationModel)
                    .enqueue(new Callback<NotificationModel>() {
                        @Override
                        public void onResponse(@NonNull Call<NotificationModel> call, @NonNull Response<NotificationModel> response) {
                        }

                        @Override
                        public void onFailure(@NonNull Call<NotificationModel> call, @NonNull Throwable t) {

                        }
                    });
        }


    }


    public interface IControlData {
        default void getUser(Users users) {
        }

        default void isOK(boolean is) {
        }

    }

}
