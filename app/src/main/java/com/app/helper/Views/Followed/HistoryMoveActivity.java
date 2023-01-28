package com.app.helper.Views.Followed;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.app.helper.R;
import com.app.helper.Utils.ProgressDialog;
import com.app.helper.Utils.UtilsClazz;
import com.app.helper.Location.Commons.DAO.LocationDAO;
import com.app.helper.Location.Zoning.DAO.LocationZoningDAO;
import com.app.helper.Followed.Model.Followed;
import com.app.helper.Location.History.Model.LocationHistory;
import com.app.helper.Location.Zoning.Model.LocationZoning;
import com.app.helper.Location.Commons.Model.MyLatLng;
import com.app.helper.Utils.ViewUtils;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CustomCap;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class HistoryMoveActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener {
    private static final int TOTAL_FLAG = 5;
    private final Activity activity = this;
    private TextView tv_date, tv_count, tv_cancel, tv_ok;
    private RelativeLayout rl_draw;
    private LinearLayout ll_date;
    private FloatingActionsMenu fam;
    private FloatingActionButton fab_notification, fab_draw;
    private GoogleMap mMap;

    private LocationDAO locationDAO;
    private LocationZoningDAO locationZoningDAO;
    private Followed followed_current;
    private LocationZoning mLocationZoning;

    private long current_selection;
    private Geocoder geocoder;
    private final List<PatternItem> pattern = Arrays.asList(
            new Dot(), new Gap(5));

    private Polygon polygon;
    private final List<LatLng> latLngList = new ArrayList<>();
    private final List<Marker> markerList = new ArrayList<>();
    private int click;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_move);
        followed_current = (Followed) getIntent().getSerializableExtra(FollowActivity.KEY_FOLLOWED);
        if (followed_current == null) finish();

        locationDAO = new LocationDAO();
        locationZoningDAO = new LocationZoningDAO();
        geocoder = new Geocoder(this, Locale.getDefault());
        initMap();

        getWidgets();
        setListeners();
    }

    private void getWidgets() {
        tv_date = findViewById(R.id.tv_date);
        tv_date.setText(UtilsClazz.convertTimeMillisToDate(System.currentTimeMillis()));
        tv_count = findViewById(R.id.tv_count);
        tv_cancel = findViewById(R.id.tv_cancel);
        tv_ok = findViewById(R.id.tv_ok);

        rl_draw = findViewById(R.id.rl_draw);

        ll_date = findViewById(R.id.ll_date);

        fam = findViewById(R.id.fam);
        fab_notification = findViewById(R.id.fab_notification);
        fab_draw = findViewById(R.id.fab_draw);
    }

    private void showDialogConfirm() {
        new MaterialAlertDialogBuilder(activity)
                .setCancelable(false)
                .setTitle("CẢNH BÁO!")
                .setIcon(R.drawable.ic_baseline_warning_24)
                .setMessage("- Khoanh vùng cũ sẽ mất đi nếu bạn chọn ĐỒNG Ý.\n- Không thể phục hồi khoanh vùng cũ.")
                .setPositiveButton("ĐỒNG Ý", (dialog, which) -> {
                    dialog.dismiss();
                    clearPolygon();
                    showOrHide(true);
                    mLocationZoning = null;
                    fab_draw.setTitle("Khoanh vùng");
                    fab_notification.setVisibility(View.GONE);
                })
                .setNegativeButton("HỦY", (dialog, which) -> {
                    dialog.dismiss();
                    fam.collapse();
                })
                .show();
    }

    private void setListeners() {
        fab_draw.setOnClickListener(v -> {
            if (mLocationZoning != null) {
                showDialogConfirm();
            } else {
                showOrHide(true);
            }
        });
        fab_notification.setOnClickListener(v -> {
            boolean isNotification = mLocationZoning.isIs_notification();
            mLocationZoning.setIs_notification(!isNotification);
            ProgressDialog.show(activity);
            locationZoningDAO.putLocationZoning(followed_current.getUid(), mLocationZoning, new LocationZoningDAO.IControlData() {
                @Override
                public void isOK(boolean is) {
                    ProgressDialog.dismiss();
                    if (is) {
                        Toast.makeText(activity, (isNotification ? "Đã tắt thông báo" : "Đã bật thông báo"), Toast.LENGTH_SHORT).show();
                        if (isNotification) {
                            runOnUiThread(() -> {
                                polygon.setStrokeColor(Color.GRAY);
                                fab_notification.setTitle("Thông báo: TẮT");
                                fab_notification.setIcon(R.drawable.ic_location_notification_off);
                            });

                        } else {
                            runOnUiThread(() -> {
                                polygon.setStrokeColor(Color.RED);
                                fab_notification.setTitle("Thông báo: BẬT");
                                fab_notification.setIcon(R.drawable.ic_location_notification_on);
                            });
                        }
                    } else
                        Toast.makeText(activity, "Đã có lỗi xảy ra!!!", Toast.LENGTH_SHORT).show();
                }
            });
        });
        tv_ok.setOnClickListener(v -> {
            showOrHide(false);
            drawPolygon();

            mLocationZoning = new LocationZoning();
            mLocationZoning.setPolygon(latLngList.stream()
                    .map(latLng -> new MyLatLng(latLng.latitude, latLng.longitude))
                    .collect(Collectors.toList()));
            ProgressDialog.show(activity);
            locationZoningDAO.putLocationZoning(followed_current.getUid(), mLocationZoning, new LocationZoningDAO.IControlData() {
                @Override
                public void isOK(boolean is) {
                    ProgressDialog.dismiss();
                    if (is) {
                        Toast.makeText(activity, "Thêm khoanh vùng thành công", Toast.LENGTH_SHORT).show();
                        runOnUiThread(() -> {
                            fab_draw.setTitle("Sửa khoanh vùng hiện tại");
                            fab_notification.setVisibility(View.VISIBLE);
                            fab_notification.setTitle("Thông báo: BẬT");
                        });
                    } else {
                        Toast.makeText(activity, "Đã có lỗi xảy ra!!!", Toast.LENGTH_SHORT).show();
                    }

                }
            });


        });
        tv_cancel.setOnClickListener(v -> {
            showOrHide(false);
            clearPolygon();
            ProgressDialog.show(activity);
            locationZoningDAO.putLocationZoning(followed_current.getUid(), null, new LocationZoningDAO.IControlData() {
                @Override
                public void isOK(boolean is) {
                    ProgressDialog.dismiss();
                    if (is)
                        Toast.makeText(activity, "Thêm khoanh vùng thành công", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(activity, "Đã có lỗi xảy ra!!!", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void showOrHide(boolean isShow) {
        if (fam.isExpanded()) fam.collapse();
        if (isShow) {
            fab_draw.setEnabled(false);
            mMap.setOnMapClickListener(this);
            rl_draw.setVisibility(View.VISIBLE);
            ll_date.setVisibility(View.GONE);
            tv_ok.setEnabled(false);
            tv_ok.setTextColor(getColor(R.color.gray));
        } else {
            fab_draw.setEnabled(true);
            mMap.setOnMapClickListener(null);
            rl_draw.setVisibility(View.GONE);
            ll_date.setVisibility(View.VISIBLE);
        }
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

    public void dateSelect(View view) {
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Chọn ngày")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .setSelection(current_selection == 0 ? System.currentTimeMillis() : current_selection)
                .setCalendarConstraints(new CalendarConstraints.Builder()
                        .setValidator(DateValidatorPointBackward.now())
                        .build())
                .build();
        picker.addOnPositiveButtonClickListener(selection -> {
                    current_selection = selection;
                    String date = UtilsClazz.convertTimeMillisToDate(selection);
                    initMarkerList(date);
                    tv_date.setText(date);
                }
        );
        picker.show(getSupportFragmentManager(), "Date picker");
    }

    private final List<Marker> markersOfDate = new ArrayList<>();
    private Polyline polyline;

    private void removeMarkerOfDate() {
        if (markersOfDate.isEmpty()) return;
        markersOfDate.forEach(Marker::remove);
        markersOfDate.clear();
        if (polyline != null)
            polyline.remove();
    }

    private void initMarkerList(String date) {
        removeMarkerOfDate();
        String uid = followed_current.getUid();
        locationDAO.getLocationHistoryByDate(uid, date, new LocationDAO.IControlData() {
            @Override
            public void dataLocationHistory(List<LocationHistory> locationHistoryList) {
                if (locationHistoryList != null) {
                    PolylineOptions polylineOptions;
                    LocationHistory first = locationHistoryList.remove(0);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(first.getLatLng(), 16));

                    Marker markerFirst = mMap.addMarker(new MarkerOptions().position(
                            first.getLatLng())
                            .title("Bắt đầu: " + first.getTime())
                            .snippet(UtilsClazz.getAddressFromLatLng(geocoder, first.getLatLng()))
                            .icon(ViewUtils.bitmapDescriptorFromVector(activity, R.drawable.ic_location_start)));
                    if (markerFirst != null) {
                        markerFirst.showInfoWindow();
                        markersOfDate.add(markerFirst);
                    }
                    if (locationHistoryList.size() > 0) {
                        polylineOptions = new PolylineOptions();
                        polylineOptions.add(first.getLatLng());
                        polylineOptions.addAll(locationHistoryList.stream().map(LocationHistory::getLatLng).collect(Collectors.toList())).clickable(true);

                        LocationHistory end = locationHistoryList.remove(locationHistoryList.size() - 1);
                        Marker markerLast = mMap.addMarker(new MarkerOptions().position(
                                end.getLatLng())
                                .title("Kết thúc:" + first.getTime())
                                .snippet(UtilsClazz.getAddressFromLatLng(geocoder, end.getLatLng()))
                                .icon(ViewUtils.bitmapDescriptorFromVector(activity, R.drawable.ic_location_end))
                        );
                        if (markerLast != null) {
                            markersOfDate.add(markerLast);
                        }
                        for (LocationHistory locationHistory : locationHistoryList) {
                            Marker marker = mMap.addMarker(new MarkerOptions().position(
                                    locationHistory.getLatLng())
                                    .title(locationHistory.getTime())
                                    .snippet(UtilsClazz.getAddressFromLatLng(geocoder, locationHistory.getLatLng()))
                                    .icon(ViewUtils.bitmapDescriptorFromVector(activity, R.drawable.ic_location_user))
                            );
                            if (marker != null) markersOfDate.add(marker);

                        }
                        polyline = mMap.addPolyline(polylineOptions);
                        polyline.setColor(Color.parseColor("#FF018786"));
                        polyline.setPattern(pattern);
                        polyline.setStartCap(new CustomCap(ViewUtils.bitmapDescriptorFromVector(activity, R.drawable.ic_baseline_keyboard_arrow_down_24)));
                        polyline.setEndCap(new CustomCap(ViewUtils.bitmapDescriptorFromVector(activity, R.drawable.ic_baseline_keyboard_arrow_up_24)));
                    }

                } else {
                    LatLng currentPlace = locationDAO.getCurrentLocation();
                    if (currentPlace == null) return;
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPlace, 16));

                    Toast.makeText(activity, "Ngày: " + date + " chưa có vị trí!", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    private void clearPolygon() {
        if (polygon != null) polygon.remove();
        for (Marker marker : markerList) {
            marker.remove();
        }
        latLngList.clear();
        markerList.clear();
    }

    private void drawPolygon() {
        if (polygon != null) polygon.remove();
        PolygonOptions polygonOptions = new PolygonOptions().addAll(latLngList).clickable(true);
        polygon = mMap.addPolygon(polygonOptions);
        polygon.setStrokeColor(Color.RED);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        Log.e("MAP READY", "****");
        mMap = googleMap;
        runOnUiThread(() -> {
            String date = UtilsClazz.convertTimeMillisToDate(System.currentTimeMillis());
            initMarkerList(date);
        });
        checkLocationZoning();
    }

    private void checkLocationZoning() {
        locationZoningDAO.getLocationZoning(followed_current.getUid(), new LocationZoningDAO.IControlData() {
            @Override
            public void locationZoning(LocationZoning locationZoning) {
                if (locationZoning != null) {
                    mLocationZoning = locationZoning;
                    initPolygonOfFollowed();

                    fab_draw.setTitle("Sửa khoanh vùng hiện tại");
                    if (!locationZoning.isIs_notification()) {
                        fab_notification.setIcon(R.drawable.ic_location_notification_off);
                        fab_notification.setTitle("Thông báo: TẮT");
                    }
                } else {
                    fab_notification.setVisibility(View.GONE);
                }

            }
        });
    }

    private void initPolygonOfFollowed() {
        if (mLocationZoning != null) {
            latLngList.addAll(mLocationZoning.convertList());
            for (LatLng latLng : latLngList) {
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(latLng)
                        .icon(ViewUtils.bitmapDescriptorFromVector(activity, R.drawable.ic_location_flag));
                Marker marker = mMap.addMarker(markerOptions);
                markerList.add(marker);
            }
            PolygonOptions polygonOptions = new PolygonOptions().addAll(latLngList).clickable(true);
            polygon = mMap.addPolygon(polygonOptions);
            polygon.setStrokeColor(mLocationZoning.isIs_notification() ? Color.RED : Color.GRAY);

        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onMapClick(@NonNull LatLng latLng) {
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .icon(ViewUtils.bitmapDescriptorFromVector(activity, R.drawable.ic_location_flag));
        Marker marker = mMap.addMarker(markerOptions);
        if (marker != null) {
            ++click;
            runOnUiThread(() -> tv_count.setText(TOTAL_FLAG - click + ""));

            latLngList.add(latLng);
            markerList.add(marker);
            if (click == TOTAL_FLAG) {
                tv_ok.setEnabled(true);
                tv_ok.setTextColor(getColor(R.color.yellow));
                click = 0;
                mMap.setOnMapClickListener(null);
            }
        }
    }
}