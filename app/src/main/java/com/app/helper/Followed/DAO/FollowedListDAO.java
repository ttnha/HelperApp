package com.app.helper.Followed.DAO;

import android.util.Log;

import androidx.annotation.NonNull;

import com.app.helper.Firebases.FireBaseInit;
import com.app.helper.Firebases.TableName;
import com.app.helper.Followed.Model.Followed;
import com.app.helper.Guardian.Model.Guardian;
import com.app.helper.Notification.Model.BaseData;
import com.app.helper.Notification.Model.DataAddGuardian;
import com.app.helper.Notification.Model.DataCallGuardian;
import com.app.helper.Notification.Model.NotificationModel;
import com.app.helper.Notification.Service.INotificationService;
import com.app.helper.Notification.Service.NotificationService;
import com.app.helper.User.Model.SessionManager;
import com.app.helper.User.Model.Users;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FollowedListDAO {
    private static final String TABLE_NAME = TableName.FollowedList.name();

    public void getFollowedList(String uid, IControlData iControlData) {
        FireBaseInit.getInstance().getReference().child(TABLE_NAME)
                .child(uid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult() != null && task.getResult().exists()) {
                            List<Followed> followedList = new ArrayList<>();
                            for (DataSnapshot dn : task.getResult().getChildren()) {
                                Followed fl = dn.getValue(Followed.class);
                                if (fl != null)
                                    followedList.add(fl);
                            }
                            iControlData.dataFollowed(followedList.isEmpty() ? null : followedList);
                            return;
                        }
                    }
                    iControlData.dataFollowed(null);
                });
    }

    public void putFollowedAfterAddGuardian(String guardian_uid, Followed followed, IControlData iControlData) {
        Log.e("**MSG", guardian_uid + followed.getUid());
        FireBaseInit.getInstance().getReference().child(TABLE_NAME)
                .child(guardian_uid)
                .child(followed.getUid())
                .setValue(followed)
                .addOnCompleteListener(task -> iControlData.isOK(task.isSuccessful()));
    }

    private int index = 0;

    public void deleteFollowed(String guardian_uid, String followed_uid, IControlData iControlData) {
        FireBaseInit.getInstance().getReference().child(TABLE_NAME)
                .child(guardian_uid)
                .child(followed_uid)
                .removeValue()
                .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // Xóa trong table User -> Guardians
                                DatabaseReference dRef = FireBaseInit.getInstance().getReference().child(TableName.Users.name())
                                        .child(followed_uid)
                                        .child("guardians");
                                dRef.get().addOnCompleteListener(task1 -> {
                                    if (task1.getResult() != null) {
                                        List<Guardian> guardians = new ArrayList<>();
                                        task1.getResult().getChildren().forEach(ds -> {
                                            Log.e("LOGGGG", ds.toString());
                                            Guardian guardian = ds.getValue(Guardian.class);
                                            if (guardian != null) {
                                                if (!guardian.getUid().equals(guardian_uid)) {
                                                    guardian.setPriority(++index);
                                                    guardians.add(guardian);
                                                }
                                            }
                                        });
                                        dRef.setValue(guardians);
                                        iControlData.isOK(true);
                                    }
                                    index = 0;
                                });
                            }
                            iControlData.isOK(task.isSuccessful());
                        }
                );
    }

    public void getFollowedByUid(String uid, IControlData iControlData) {
        FireBaseInit.getInstance().getReference()
                .child(TableName.Users.name())
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

    public void sendNotification(DataAddGuardian dataAddGuardian, Users currentUser, IControlData iControlData) {
        FireBaseInit.getInstance().getReference()
                .child(TableName.Users.name())
                .child(dataAddGuardian.getUid()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult() != null) {
                    Users users = task.getResult().getValue(Users.class);
                    if (users != null) {
                        String token = users.getToken();
                        dataAddGuardian.setName(users.getName());
                        dataAddGuardian.setContent(currentUser.getUid() + " muốn thêm bạn vào danh sách thành viên.");

                        NotificationModel notificationModel = new NotificationModel();
                        notificationModel.setTo(token);

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
                    }

                }
            }
        });

    }

    public boolean isGuardian(String phoneNumber, List<Guardian> guardianList) {
        if (guardianList != null && !guardianList.isEmpty())
            for (Guardian guardian : guardianList) {
                if (guardian.getUid().equals(phoneNumber)) return true;
            }
        return false;
    }

    public interface IControlData {
        default void dataFollowed(List<Followed> followedList) {
        }

        default void isOK(boolean is) {
        }

        default void getUser(Users users) {
        }
    }
}
