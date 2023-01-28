package com.app.helper.User.DAO;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.app.helper.Firebases.FireBaseInit;
import com.app.helper.Firebases.TableName;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class ListenersCallbacksDAO {
    private static final String TABLE_NAME = TableName.ListenersCallbacks.name();
    public static final int STATUS_WAITING = 0;
    public static final int STATUS_CANCEL = 1;
    public static final int STATUS_ACCEPT = 2;
    private ValueEventListener valueEventListener;


    private DatabaseReference currentRef;

    // Lắng nghe sự kiện giữa các devices
    public void createListenersCallbacks(String code, IControlData iControlData) {
        FireBaseInit.getInstance().getReference()
                .child(TABLE_NAME)
                .child(code)
                .setValue(STATUS_WAITING)
                .addOnCompleteListener(task -> iControlData.isOk(task.isSuccessful()));
    }

    public void checkStatusOnChange(String code, IControlData iControlData) {
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int status = Integer.parseInt(Objects.requireNonNull(snapshot.getValue()).toString());
                    iControlData.sendStatus(status);
                } else {
                    iControlData.sendStatus(STATUS_CANCEL);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        currentRef = FireBaseInit.getInstance().getReference()
                .child(TABLE_NAME)
                .child(code);

        currentRef.addValueEventListener(valueEventListener);

    }

    public void setStatus(int status) {
        currentRef.setValue(status);
        removeListener();
    }

    public void removeValue() {
        currentRef.removeValue();
    }

    public void removeListener() {
        currentRef.removeEventListener(valueEventListener);
    }

    public interface IControlData {
        default void sendStatus(int status) {
        }

        default void isOk(boolean is) {
        }
    }
}
