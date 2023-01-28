package com.app.helper.User.Model;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.app.helper.Location.Zoning.Model.DataRequestLocationZoning;
import com.app.helper.MyApplication.MyApplication;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;

public class SessionManager {
    private final SharedPreferences session;
    private final SharedPreferences.Editor editor;

    private static final String KEY_OTP = "OTP";
    private static final String KEY_USER = "USER";
    private static final String KEY_TOKEN = "TOKEN";

    // Location
    private static final String KEY_LOCATION = "LOCATION";
    private static final String KEY_LOCATION_REQUEST = "LOCATION_REQUEST";


    private static SessionManager INSTANCE;

    @SuppressLint("CommitPrefEdits")
    private SessionManager() {
        session = MyApplication.SESSION;
        editor = session.edit();
    }

    public static SessionManager getINSTANCE() {
        if (INSTANCE == null) {
            INSTANCE = new SessionManager();
        }
        return INSTANCE;
    }

    // OTP
    public void createOTPSession(String OTP) {
        editor.putString(KEY_OTP, OTP);
        editor.apply();
    }

    public String getOTPSession() {
        return session.getString(KEY_OTP, null);
    }

    public void removeOTPSession() {
        editor.remove(KEY_OTP);
        editor.apply();
    }

    // User
    public void createUserSession(Users users) {
        editor.putString(KEY_USER, new Gson().toJson(users));
        editor.apply();
    }

    public Users getUserSession() {
        String userJson = session.getString(KEY_USER, null);
        if (TextUtils.isEmpty(userJson)) return null;
        return new Gson().fromJson(userJson, Users.class);
    }

    // Forgot password
    private static final String KEY_FP_FLAG = "KEY_FP_FLAG";

    public void setForgotPasswordFlag(boolean value) {
        editor.putBoolean(KEY_FP_FLAG, value);
        editor.apply();
    }

    public boolean isForgotPasswordFlag() {
        return session.getBoolean(KEY_FP_FLAG, false);
    }

    // Remember Password
    private static final String KEY_REMEMBER_FLAG = "KEY_REMEMBER_FLAG";

    public void setRememberPasswordFlag(boolean value) {
        editor.putBoolean(KEY_REMEMBER_FLAG, value);
        editor.apply();
    }

    public boolean isRememberPasswordFlag() {
        return session.getBoolean(KEY_REMEMBER_FLAG, false);
    }

    // Token Notification
    public void createTokenSession(String token) {
        editor.putString(KEY_TOKEN, token);
        editor.apply();
    }

    public String getTokenSession() {
        return session.getString(KEY_TOKEN, "");
    }

    // Location
    public void createOrUpdateLocationSession(LatLng latLng) {
        editor.putString(KEY_LOCATION, new Gson().toJson(latLng));
        editor.apply();
    }

    public LatLng getLocationSession() {
        String lo = session.getString(KEY_LOCATION, null);
        return lo == null ? null : new Gson().fromJson(lo, LatLng.class);
    }

    public void setRequestLocationBackground(boolean value) {
        editor.putBoolean(KEY_LOCATION_REQUEST, value);
        editor.apply();
    }

    public boolean isLocationBackgroundRequested() {
        return session.getBoolean(KEY_LOCATION_REQUEST, false);
    }

    // Location Zoning
    private static final String KEY_LOCATION_ZONING = "KEY_LOCATION_ZONING";

    public void createOrUpdateLocationZoningMap(DataRequestLocationZoning.Data data) {
        HashMap<String, DataRequestLocationZoning.Data> dataHashMap = getLocationZoningMap();
        if (dataHashMap != null) {
            if (data.getPolygon() != null) {
                dataHashMap.put(data.getUid(), data);
                Log.e("createOrUpdateLocationZoningMap","update");
            } else { // Nếu Polygon null thì xóa người này khỏi map data
                dataHashMap.remove(data.getUid());
                Log.e("createOrUpdateLocationZoningMap","del");
            }
        } else {
            Log.e("createOrUpdateLocationZoningMap","adđ");
            dataHashMap = new HashMap<>();
            dataHashMap.put(data.getUid(), data);
        }
        editor.putString(KEY_LOCATION_ZONING, new Gson().toJson(dataHashMap));
        editor.commit();
    }

    public HashMap<String, DataRequestLocationZoning.Data> getLocationZoningMap() {
        String lzString = session.getString(KEY_LOCATION_ZONING, null);
        if (lzString != null) {
            Type type = new TypeToken<HashMap<String, DataRequestLocationZoning.Data>>() {
            }.getType();
            return new Gson().fromJson(lzString, type);
        }
        return null;
    }

    // FollowedUid using in Alarm Broadcast
    private static final String KEY_FOLLOW_ID_AB = "KEY_FOLLOW_ID_AB";

    public void createOrUpdateFollowedIdABMap(String fpId, String followId) {
        HashMap<String, String> dataMap = getFollowedIdABMap();
        if (dataMap == null) {
            dataMap = new HashMap<>();
        }
        dataMap.put(fpId, followId);
        editor.putString(KEY_FOLLOW_ID_AB, new Gson().toJson(dataMap));
        editor.commit();
    }

    public String getFollowIdByFPID(String fpId) {
        HashMap<String, String> dataMap = getFollowedIdABMap();
        if (dataMap != null && dataMap.size() != 0) {
            return dataMap.get(fpId);
        }
        return null;
    }

    public void removeFollowIdByFPID(String fpId) {
        HashMap<String, String> dataMap = getFollowedIdABMap();
        if (dataMap != null && dataMap.size() != 0) {
            dataMap.remove(fpId);
        }
        editor.putString(KEY_FOLLOW_ID_AB, new Gson().toJson(dataMap));
        editor.apply();
    }

    private HashMap<String, String> getFollowedIdABMap() {
        String str = session.getString(KEY_FOLLOW_ID_AB, null);
        if (str != null) {
            Type type = new TypeToken<HashMap<String, String>>() {
            }.getType();
            return new Gson().fromJson(str, type);
        }
        return null;
    }

    public void clearSession() {
        editor.remove(KEY_OTP);
        editor.remove(KEY_USER);
        editor.remove(KEY_FP_FLAG);
        editor.remove(KEY_REMEMBER_FLAG);
        editor.commit();
    }
}
