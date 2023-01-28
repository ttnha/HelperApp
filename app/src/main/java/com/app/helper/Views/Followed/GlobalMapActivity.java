package com.app.helper.Views.Followed;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.app.helper.R;
import com.app.helper.Utils.UtilsClazz;
import com.app.helper.Location.History.DAO.GlobalMapDAO;
import com.app.helper.Followed.Model.Followed;
import com.app.helper.Location.Commons.Model.MyLatLng;
import com.app.helper.Utils.ViewUtils;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class GlobalMapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private final Activity activity = this;
    private GoogleMap mMap;
    private Geocoder geocoder;
    private List<Followed> followedList;

    private List<GlobalMapDAO> globalMapDAOList;
    private Map<String, Marker> markerMap;
    private Map<String, LatLng> latLngMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_global_map);
        Intent intent = getIntent();
        String followedListString = intent.getStringExtra(FollowActivity.KEY_FOLLOWED_LIST);
        if (followedListString != null) {
            followedList = convertStringToList(followedListString);

            geocoder = new Geocoder(this, Locale.getDefault());
            initMap();
        } else {
            finish();
        }
    }


    private void initListenerCallBacks() {
        markerMap = new HashMap<>();
        latLngMap = new HashMap<>();
        globalMapDAOList = new ArrayList<>();

        for (Followed followed : followedList) {
            GlobalMapDAO globalMapDAO = new GlobalMapDAO();
            globalMapDAOList.add(globalMapDAO);

            String uid = followed.getUid();
            globalMapDAO.getCurrentLocationByUid(uid, new GlobalMapDAO.IControlData() {
                @Override
                public void currentLocation(MyLatLng myLatLng) {
                    LatLng latLng = myLatLng.toLatLng();
                    if (!markerMap.containsKey(uid)) {
                        Marker marker = mMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .snippet(UtilsClazz.getAddressFromLatLng(geocoder, latLng))
                                .icon(ViewUtils.bitmapDescriptorFromVector(activity, R.drawable.ic_location_user))
                                .title(followed.getName()));
                        if (marker != null) {
                            markerMap.put(uid, marker);
                            latLngMap.put(uid, latLng);
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                        }
                    } else {
                        Marker marker = markerMap.get(uid);
                        if (marker != null) {
                            marker.remove();
                            Marker marker_new = mMap.addMarker(new MarkerOptions()
                                    .position(latLng)
                                    .snippet(UtilsClazz.getAddressFromLatLng(geocoder, latLng))
                                    .icon(ViewUtils.bitmapDescriptorFromVector(activity, R.drawable.ic_location_user))
                                    .title(followed.getName()));
                            if (marker_new != null) {
                                moveCameraAnimation(latLng);
                                markerMap.put(uid, marker_new);
                                latLngMap.put(uid, latLng);
                            }
                        }
                    }

                }
            });
        }
        initSpeedDialFAB(followedList);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void initSpeedDialFAB(List<Followed> followedList) {
        FloatingActionsMenu fam = findViewById(R.id.fam);
        for (Followed followed : followedList) {
            @SuppressLint("InflateParams")
            FloatingActionButton floatingActionButton = (FloatingActionButton) getLayoutInflater().inflate(R.layout.layout_fab, null);
            floatingActionButton.setIconDrawable(getDrawable(R.drawable.ic_baseline_my_location_24));
            floatingActionButton.setTitle(followed.getName());
            floatingActionButton.setColorNormal(Color.WHITE);
            floatingActionButton.setOnClickListener(v -> {
                Objects.requireNonNull(markerMap.get(followed.getUid())).showInfoWindow();
                moveCameraAnimation(latLngMap.get(followed.getUid()));
            });
            fam.addButton(floatingActionButton);
        }
    }

    private void moveCameraAnimation(LatLng latLng) {
        mMap.animateCamera(CameraUpdateFactory.zoomIn());

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)      // Sets the center of the map to Mountain View
                .zoom(17)                   // Sets the zoom
                .build();                   // Creates a CameraPosition from the builder
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private void removeListenerCallBacks() {
        globalMapDAOList.forEach(GlobalMapDAO::removeListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (globalMapDAOList != null)
            removeListenerCallBacks();
    }

    public void back(View view) {
        finish();
    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null)
            mapFragment.getMapAsync(this);
    }

    private List<Followed> convertStringToList(String value) {
        List<Followed> followedList;
        if (value != null) {
            Type type = new TypeToken<List<Followed>>() {
            }.getType();
            followedList = new Gson().fromJson(value, type);
            return followedList;
        }
        return null;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        initListenerCallBacks();
    }

}