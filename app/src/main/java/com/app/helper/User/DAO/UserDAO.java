package com.app.helper.User.DAO;

import android.app.Activity;
import android.os.Handler;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationManagerCompat;

import com.app.helper.Firebases.FireBaseInit;
import com.app.helper.Firebases.TableName;
import com.app.helper.User.Model.SessionManager;
import com.app.helper.User.Model.Users;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.mindrot.jbcrypt.BCrypt;

import java.util.concurrent.TimeUnit;

public class UserDAO {

    private final Activity activity;
    private static final String TABLE_NAME = TableName.Users.name();

    private static SessionManager session;

    public UserDAO(Activity activity) {
        this.activity = activity;
        session = SessionManager.getINSTANCE();
    }

    public void register(Users users, IControlData iControlData) {
        users.setPwd(BCrypt.hashpw(users.getPwd(), BCrypt.gensalt(12)));
        FireBaseInit.getInstance().getReference()
                .child(TABLE_NAME)
                .child(users.getUid())
                .setValue(users)
                .addOnCompleteListener(task -> iControlData.isSuccess(task.isSuccessful()));
    }

    public void sendOTP(String phoneNumber) {
        // 0337631761 -> +84337631761
        String phoneNumberFormat = "+84" + (phoneNumber.charAt(0) == '0' ? phoneNumber : phoneNumber.substring(1));

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumberFormat,
                60L,
                TimeUnit.SECONDS,
                activity,
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                        session.createOTPSession(phoneAuthCredential.getSmsCode());
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {

                    }

                }
        );
        new Handler().postDelayed(() -> {
            // Tạm thời để đây test, phòng trường hợp không nhận được OTP
            if (TextUtils.isEmpty(session.getOTPSession())) {
                session.createOTPSession("123456");
            }
        }, 5 * 1000);
    }

    // Change Password
    public void changePassword(String currentPass, String newPassword, IControlData iControlData) {
        Users users = getUsersSession();
        if (users != null) {
            String uid = users.getUid();
            DatabaseReference mRef = FireBaseInit.getInstance().getReference()
                    .child(TABLE_NAME)
                    .child(uid);
            mRef.get().addOnCompleteListener(task -> {
                if (task.getResult().exists()) {
                    Users usersDB = task.getResult().getValue(Users.class);
                    if (usersDB != null) {
                        if (isValidPassword(usersDB.getPwd(), currentPass)) {
                            String newPass = BCrypt.hashpw(newPassword, BCrypt.gensalt(12));
                            mRef.child("pwd")
                                    .setValue(newPass)
                                    .addOnCompleteListener(task2 -> iControlData.isSuccess(true));
                        } else {
                            iControlData.isSuccess(false);
                        }
                    }
                }
            });
        }
    }

    // Forgot Password
    public void forgotPassword() {
        Users users = getUsersSession();
        if (users != null) {
            String phoneNumber = users.getUid();
            String phoneNumberFormat = "+84" + phoneNumber.substring(1);
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    phoneNumberFormat,
                    60L,
                    TimeUnit.SECONDS,
                    activity,
                    new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                        @Override
                        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                            String code = phoneAuthCredential.getSmsCode();
                            session.createOTPSession(code);
                            setPasswordForgot(phoneNumber, code);
                        }

                        @Override
                        public void onVerificationFailed(@NonNull FirebaseException e) {

                        }

                    }
            );
            // Trong vòng 10s, nếu OTP chưa về thì set tạm giá trị này. Phòng trường hợp limited OTP trong ngày
            new Handler().postDelayed(() -> {
                // Tạm thời để đây test, phòng trường hợp không nhận được OTP
                if (TextUtils.isEmpty(session.getOTPSession())) {
                    setPasswordForgot(phoneNumber, "123456");
                } else {
                    session.removeOTPSession();
                }
            }, 10 * 60_000);
        }
    }

    private void setPasswordForgot(String uid, String password) {
        password = BCrypt.hashpw(password, BCrypt.gensalt(12));
        FireBaseInit.getInstance().getReference()
                .child(TABLE_NAME)
                .child(uid)
                .child("pwd")
                .setValue(password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                session.setForgotPasswordFlag(true);
            }
        });
    }

    public void setForgotPasswordFlag() {
        session.setForgotPasswordFlag(false);
    }

    public boolean isForgotPasswordFlag() {
        return session.isForgotPasswordFlag();
    }

    // Remember Password
    public void setRememberPasswordFlag(boolean value) {
        session.setRememberPasswordFlag(value);
    }

    public boolean isRememberPasswordFlag() {
        return session.isRememberPasswordFlag();
    }

    private static DatabaseReference mRefLogin;

    public boolean isValidPassword(String realPass, String pass) {
        return BCrypt.checkpw(pass, realPass);
    }

    public void login(String pwd, IControlData iControlData) {
        Users userSession = getUserSession();
        if (userSession == null) {
            iControlData.isSuccess(false);
        } else {
            String uid = userSession.getUid();
            // Check Pwd
            mRefLogin = FireBaseInit.getInstance().getReference()
                    .child(TABLE_NAME)
                    .child(uid);
            mRefLogin.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (task.getResult() != null) {
                        Users users = task.getResult().getValue(Users.class);
                        if (users != null) {
                            boolean isPasswordValid = isValidPassword(users.getPwd(), pwd);
                            if (isPasswordValid) {
                                iControlData.isSuccess(true);
                                // Lưu notification token vào DB
                                String token = session.getTokenSession();
                                mRefLogin.child("token").setValue(token);
                                // Lắng nghe sự kiện mỗi lần tác động lên user
                                mRefLogin.addValueEventListener(loginListenerCallback);
                                return;
                            }
                        }
                    }
                }
                iControlData.isSuccess(false);
            });
        }
    }

    public static void loginWithRemember() {
        Users userSession = SessionManager.getINSTANCE().getUserSession();
        if (userSession != null) {
            String uid = userSession.getUid();
            mRefLogin = FireBaseInit.getInstance().getReference()
                    .child(TABLE_NAME)
                    .child(uid);
            mRefLogin.addValueEventListener(loginListenerCallback);
        }
    }

    // Hàm kiểm tra số điện thoại đã có trong hệ thống hay chưa
    public void isExistsPhoneNumber(String phoneNumber, IControlData iControlData) {
        FireBaseInit.getInstance().getReference().child(TABLE_NAME)
                .child(phoneNumber)
                .get()
                .addOnSuccessListener(task -> { // lambda
                    if (task.exists()) {
                        // Nếu tồn tại thì tại đây mình lưu thằng user này vào Session luôn
                        Users users = task.getValue(Users.class);
                        if (users != null) {
                            iControlData.isSuccess(true);
                            session.createUserSession(users);
                        } else {
                            iControlData.isSuccess(false);
                        }
                    } else {
                        iControlData.isSuccess(false);
                    }
                });
    }

    // Hàm callbacks khi user đó có sự thay đổi về mặt dữ liệu
    private static final ValueEventListener loginListenerCallback = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if (dataSnapshot.exists()) {
                Users users = dataSnapshot.getValue(Users.class);
                if (users != null) {
                    session.createUserSession(dataSnapshot.getValue(Users.class));
                }

            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
        }
    };


    public static Users getUsersSession() {
        return SessionManager.getINSTANCE().getUserSession();
    }

    public Users getUserSession() {
        return session.getUserSession();
    }

    public void removeEventListener() {
        mRefLogin.removeEventListener(loginListenerCallback);
    }

    public void clearSession() {
        NotificationManagerCompat.from(activity).cancelAll();
        session.clearSession();
    }

    public interface IControlData {
        default void isSuccess(boolean is) {
        }
    }
}
