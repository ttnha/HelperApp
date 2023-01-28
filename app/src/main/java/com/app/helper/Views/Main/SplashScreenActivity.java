package com.app.helper.Views.Main;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.app.helper.R;
import com.app.helper.User.DAO.UserDAO;
import com.app.helper.User.Model.SessionManager;
import com.app.helper.Views.Security.LoginActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

public class SplashScreenActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_LOCATION_ANDROID11_1 = 11;
    private static final int REQUEST_CODE_LOCATION_ANDROID11_2 = 112;

    private static final int REQUEST_CODE_LOCATION_ANDROID10 = 77;
    private static final int REQUEST_CODE_LOCATION_ANDROID8 = 88;
    private static final int TIME_DELAY = 2 * 1000;
    private static final int TIME_INTERVAL = 10 * 1000; // 10 seconds
    private static final int TIME_FASTEST_INTERVAL = TIME_INTERVAL / 2; // 5 seconds

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        checkPermissionLocation();
    }

    private void checkPermissionLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if(Build.VERSION.SDK_INT == Build.VERSION_CODES.Q){ // Android 10
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    goHome();
                } else {
                    ActivityCompat.requestPermissions(
                            this,
                            new String[]{
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                            },
                            REQUEST_CODE_LOCATION_ANDROID10
                    );
                }
            }else{ // Android 11
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    goHome();
                } else {
                    ActivityCompat.requestPermissions(
                            this,
                            new String[]{
                                    Manifest.permission.ACCESS_FINE_LOCATION
                            },
                            REQUEST_CODE_LOCATION_ANDROID11_1
                    );
                }
            }

        } else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                goHome();
            } else {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_CODE_LOCATION_ANDROID8
                );
            }

        }
    }

    @SuppressLint("MissingPermission")
    private void goHome() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location location = locationResult.getLastLocation();
                SessionManager.getINSTANCE()
                        .createOrUpdateLocationSession(new LatLng(location.getLatitude(), location.getLongitude()));
                fusedLocationProviderClient.removeLocationUpdates(locationCallback);
            }
        };
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(TIME_INTERVAL);
        locationRequest.setFastestInterval(TIME_FASTEST_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());

        new Handler().postDelayed(() -> {
                startActivity(new Intent(SplashScreenActivity.this, LoginActivity.class));
            finish();
        }, TIME_DELAY);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_LOCATION_ANDROID10) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                goHome();
            } else {
                finish();
            }
        } else if (requestCode == REQUEST_CODE_LOCATION_ANDROID11_1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{
                                Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        },
                        REQUEST_CODE_LOCATION_ANDROID11_2
                );
            } else {
                finish();
            }
        }
        else if(requestCode == REQUEST_CODE_LOCATION_ANDROID11_2){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
              goHome();
            } else {
                finish();
            }
        }
        else if (requestCode == REQUEST_CODE_LOCATION_ANDROID8) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                goHome();
            } else {
                finish();
            }
        }
    }
}