package com.app.helper.Location.FamiliarPlace.DAO;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.app.helper.Firebases.FireBaseInit;
import com.app.helper.Firebases.TableName;
import com.app.helper.Followed.Model.Followed;
import com.app.helper.Location.Commons.Broadcasts.AlarmBroadcast;
import com.app.helper.Location.FamiliarPlace.Model.FamiliarPlace;
import com.app.helper.User.DAO.UserDAO;
import com.app.helper.User.Model.SessionManager;
import com.app.helper.User.Model.Users;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.Calendar;
import java.util.List;

public class FamiliarPlaceDAO {
    private static final String TABLE_NAME = TableName.FamiliarPlace.name();

    private final Users mUser;
    private final Context mContext;
    private final Followed mFollowed;
    private final AlarmBroadcast mAlarmBroadcast;

    public FamiliarPlaceDAO(Context mContext, Followed mFollowed) {
        this.mUser = UserDAO.getUsersSession();
        this.mContext = mContext;
        this.mFollowed = mFollowed;
        this.mAlarmBroadcast = new AlarmBroadcast();
    }

    // START: getFamiliarPlace
    private DatabaseReference mRef;
    private IControlData mIControlData;

    public void getFamiliarPlace(IControlData iControlData) {
        if (mUser != null) {
            String currentUid = mUser.getUid();
            mRef = FireBaseInit.getInstance().getReference()
                    .child(TABLE_NAME)
                    .child(currentUid)
                    .child(mFollowed.getUid());
            mIControlData = iControlData;
            mIControlData.familiarPlace(null);
            mRef.addChildEventListener(childEventListener);
        } else {
            iControlData.familiarPlace(null);
        }

    }

    private final ChildEventListener childEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            if (snapshot.exists()) {
                FamiliarPlace familiarPlace = snapshot.getValue(FamiliarPlace.class);
                if (familiarPlace != null)
                    mIControlData.familiarPlace(familiarPlace);
            }
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot snapshot) {
            if (snapshot.exists()) {
                FamiliarPlace familiarPlace = snapshot.getValue(FamiliarPlace.class);
                if (familiarPlace != null) {
                    mAlarmBroadcast.cancelAlarm(mContext, Integer.parseInt(familiarPlace.getId()));
//                    Log.e("onChildRemoved", familiarPlace.getId());
                }
            }
        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
        }
    };

    public void removeListener() {
        if (mRef != null) mRef.removeEventListener(childEventListener);
    }
    // END: getFamiliarPlace

    // START: putFamiliarPlaces
    public void updateValues(List<FamiliarPlace> familiarPlaces) {
        if (mUser != null) {
            String currentUid = mUser.getUid();
            DatabaseReference ref = FireBaseInit.getInstance().getReference()
                    .child(TABLE_NAME)
                    .child(currentUid)
                    .child(mFollowed.getUid());
            familiarPlaces.forEach(fp -> {
                ref.child(fp.getId()).setValue(fp);
                int id = Integer.parseInt(fp.getId());
                mAlarmBroadcast.cancelAlarm(mContext, id);
                Log.e(fp.getId(), fp.isIs_interval() + "");
                if (fp.isStatus()) {
                    mAlarmBroadcast.setAlarm(mContext
                            , getTimeMillis(fp.getFp_date().getDate(), fp.getFp_date().getTime_start()),
                            fp, mFollowed.getName());
                }
            });
        }
    }

    // START: putFamiliarPlaces
    public void addValue(FamiliarPlace familiarPlace, IControlData iControlData) {
        if (mUser != null) {
            String currentUid = mUser.getUid();
            FireBaseInit.getInstance().getReference()
                    .child(TABLE_NAME)
                    .child(currentUid)
                    .child(mFollowed.getUid())
                    .child(familiarPlace.getId())
                    .setValue(familiarPlace)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            SessionManager.getINSTANCE().createOrUpdateFollowedIdABMap(familiarPlace.getId(), mFollowed.getUid());
                            iControlData.isOK(true);
                            mAlarmBroadcast.setAlarm(mContext
                                    , getTimeMillis(familiarPlace.getFp_date().getDate(), familiarPlace.getFp_date().getTime_start()), familiarPlace, mFollowed.getName());
                        } else {
                            iControlData.isOK(false);

                        }
                    });
        } else
            iControlData.isOK(false);
    }
    // END: putFamiliarPlaces

    private long getTimeMillis(String date, String time) {
        String[] dateArr = date.split("-");
        String[] timeArr = time.split(":");

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, Integer.parseInt(dateArr[0]));
        calendar.set(Calendar.MONTH, Integer.parseInt(dateArr[1]) - 1);
        calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dateArr[2]));

        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeArr[0]));
        calendar.set(Calendar.MINUTE, Integer.parseInt(timeArr[1]));
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTimeInMillis();
    }
    // END: putFamiliarPlaces

    // START: removeValue
    public void removeValue(String id, IControlData iControlData) {
        if (mUser != null) {
            String currentUid = mUser.getUid();
            FireBaseInit.getInstance().getReference()
                    .child(TABLE_NAME)
                    .child(currentUid)
                    .child(mFollowed.getUid())
                    .child(id)
                    .removeValue() // Xóa value
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            SessionManager.getINSTANCE().removeFollowIdByFPID(id);
                            // Xóa thành công thì xóa lịch thông báo AlarmBroadCast
                            try {
                                int id_al_br = Integer.parseInt(id);
                                mAlarmBroadcast.cancelAlarm(mContext, id_al_br);
                                iControlData.isOK(true);
                            } catch (NumberFormatException e) {
                                Log.e("MSG-ERR", e.getMessage());
                                iControlData.isOK(false);

                            }
                            //
                            return;
                        }
                        iControlData.isOK(false);
                    });
        }
    }
    // END: removeValue

    public LatLng getCurrentLocation() {
        return SessionManager.getINSTANCE().getLocationSession();
    }


    public interface IControlData {
        default void familiarPlace(FamiliarPlace familiarPlace) {
        }

        default void isOK(boolean isOK) {
        }
    }
}
