package com.app.helper.Location.History.Model;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.app.helper.Location.Commons.Broadcasts.LocationBroadcast;
import com.app.helper.Location.Commons.DAO.LocationDAO;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class LocationHelper {

//    private static final long UPDATE_INTERVAL = 20 * 60 * 1000; // Every 20 minutes.
//
//    private static final long FASTEST_UPDATE_INTERVAL = 15 * 60 * 1000; // Every 15 minutes.
//
//    private static final long MAX_WAIT_TIME = UPDATE_INTERVAL; // Every 20 minutes.

    private static final long UPDATE_INTERVAL = 1 * 60 * 1000; // Every 3 minutes.

    private static final long FASTEST_UPDATE_INTERVAL = 1 * 60 * 1000; // Every 2 minutes.

    private static final long MAX_WAIT_TIME = UPDATE_INTERVAL; // Every 3 minutes.


    private static final String TAG = "LocationHelper";

    private LocationRequest mLocationRequest;

    private FusedLocationProviderClient mFusedLocationClient;

    private final LocationDAO locationDAO;
    private final Context context;

    public LocationHelper(Context context) {
        this.context = context;
        locationDAO = new LocationDAO();
    }

    @SuppressLint("MissingPermission")
    public void requestLocationUpdates() {
        try {
            Log.e(TAG, "Starting location updates");
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
            createLocationRequest();
            locationDAO.setRequestLocationBackground(true);
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, getPendingIntent());
        } catch (SecurityException e) {
            locationDAO.setRequestLocationBackground(false);
            e.printStackTrace();
        }
    }

    public void removeLocationUpdates() {
        Log.e(TAG, "Removing location updates");
        locationDAO.setRequestLocationBackground(false);
        mFusedLocationClient.removeLocationUpdates(getPendingIntent());
    }

    private void createLocationRequest() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setMaxWaitTime(MAX_WAIT_TIME);
    }

    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(context, LocationBroadcast.class);
        intent.setAction(LocationBroadcast.ACTION_PROCESS_UPDATES);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }


}
