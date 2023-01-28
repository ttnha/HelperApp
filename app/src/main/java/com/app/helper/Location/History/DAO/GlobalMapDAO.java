package com.app.helper.Location.History.DAO;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.app.helper.Firebases.FireBaseInit;
import com.app.helper.Firebases.TableName;
import com.app.helper.Location.Commons.Model.MyLatLng;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;

public class GlobalMapDAO implements Serializable {
    public static final String TABLE_NAME = TableName.LocationHistory.name();
    private static final DateTimeFormatter dft = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private DatabaseReference mRef;

    private IControlData mIControlData;

    public void getCurrentLocationByUid(String uid, IControlData iControlData) {
        mRef = FireBaseInit.getInstance().getReference()
                .child(TABLE_NAME)
                .child(uid);
        mRef.get().addOnSuccessListener(dataSnapshot -> {
            if (dataSnapshot.exists()) {
                Iterator<DataSnapshot> dataSnapshotIterator = dataSnapshot.getChildren().iterator();
                DataSnapshot lastElement = dataSnapshotIterator.next();
                while (dataSnapshotIterator.hasNext()) {
                    lastElement = dataSnapshotIterator.next();
                }
                lastTime = LocalDateTime.parse(lastElement.getKey(), dft);
                MyLatLng myLatLng = lastElement.getValue(MyLatLng.class);

                mRef.addChildEventListener(childEventListener);
                iControlData.currentLocation(myLatLng);
                mIControlData = iControlData;
            } else {
                iControlData.currentLocation(null);
            }
        });

    }

    private LocalDateTime lastTime;
    private final ChildEventListener childEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            if (mIControlData != null) {
                LocalDateTime localDateTime = LocalDateTime.parse(snapshot.getKey(), dft);
                if (localDateTime.isAfter(lastTime)) {
                    MyLatLng myLatLng = snapshot.getValue(MyLatLng.class);
                    mIControlData.currentLocation(myLatLng);
                }
            }
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot snapshot) {

        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };

    public void removeListener() {
        mRef.removeEventListener(childEventListener);
    }

    public interface IControlData {
        default void currentLocation(MyLatLng myLatLng) {
        }
    }

}
