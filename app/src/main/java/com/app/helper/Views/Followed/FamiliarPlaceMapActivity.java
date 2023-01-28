package com.app.helper.Views.Followed;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.app.helper.Location.FamiliarPlace.DAO.FamiliarPlaceDAO;
import com.app.helper.Location.FamiliarPlace.Model.FPDate;
import com.app.helper.Location.FamiliarPlace.Model.FamiliarPlace;
import com.app.helper.R;
import com.app.helper.Utils.UtilsClazz;
import com.app.helper.Utils.ViewUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.slider.Slider;

import java.util.Calendar;
import java.util.Locale;

public class FamiliarPlaceMapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private final Activity activity = this;
    private static final int DEFAULT_RADIUS = 50;
    private TextView tv_place, tv_date, tv_time_start, tv_time_end, tv_radius;
    private Slider sl_radius;
    private Button btn_confirm;
    private RelativeLayout rl_bt_s;
    private BottomSheetBehavior<View> bottomSheetBehavior;

    private FamiliarPlaceDAO familiarPlaceDAO;
    private FamiliarPlace mFamiliarPlace;
    private int index;

    private GoogleMap mMap;
    private Geocoder geocoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_familiar_place_map);
        mFamiliarPlace = (FamiliarPlace) getIntent().getSerializableExtra(FamiliarPlaceActivity.KEY_DATA);
        index = getIntent().getIntExtra(FamiliarPlaceActivity.KEY_INDEX, -1);
        familiarPlaceDAO = new FamiliarPlaceDAO(this, null);
        geocoder = new Geocoder(this, Locale.getDefault());

        getWidgets();
        setListeners();
        initMap();
        runOnUiThread(() -> {
            initBottomSheet();
            initDataTextView();
        });

    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null)
            mapFragment.getMapAsync(this);
    }

    private void getWidgets() {
        tv_place = findViewById(R.id.tv_place);
        tv_date = findViewById(R.id.tv_date);
        tv_time_start = findViewById(R.id.tv_time_start);
        tv_time_end = findViewById(R.id.tv_time_end);
        tv_radius = findViewById(R.id.tv_radius);

        sl_radius = findViewById(R.id.sl_radius);

        btn_confirm = findViewById(R.id.btn_confirm);

        rl_bt_s = findViewById(R.id.rl_bt_s);
    }

    private int year, month, day;
    private int s_lastHour, s_lastMinute, e_lastHour, e_lastMinute;
    private int radius = DEFAULT_RADIUS;

    private void initDataTextView() {
        s_lastHour = s_lastMinute = e_lastHour = e_lastMinute = -1;
        if (mFamiliarPlace == null) {
            Calendar calendar = Calendar.getInstance();
            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH);
            day = calendar.get(Calendar.DAY_OF_MONTH);

            s_lastHour = calendar.get(Calendar.HOUR_OF_DAY);
            e_lastHour = s_lastHour == 23 ? s_lastHour : s_lastHour + 1;
            s_lastMinute = e_lastMinute = calendar.get(Calendar.MINUTE);

            tv_date.setText(initDate(year, month + 1, day));
            tv_time_start.setText(initTime(s_lastHour, s_lastMinute));
            tv_time_end.setText(initTime(e_lastHour, e_lastMinute));

            tv_radius.setText(initRadius(radius));
        } else {
            tv_place.setText(mFamiliarPlace.getPlace_name());
            FPDate fpDate = mFamiliarPlace.getFp_date();
            int[] date = fpDate.convertDate();
            int[] time_start = fpDate.convertTime(true);
            int[] time_end = fpDate.convertTime(false);

            year = date[0];
            month = date[1] - 1;
            day = date[2];

            s_lastHour = time_start[0];
            s_lastMinute = time_start[1];

            e_lastHour = time_end[0];
            e_lastMinute = time_end[1];

            tv_date.setText(initDate(year, month + 1, day));
            tv_time_start.setText(initTime(s_lastHour, s_lastMinute));
            tv_time_end.setText(initTime(e_lastHour, e_lastMinute));

        }
    }

    private void setListeners() {
        tv_date.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog
                    (this, (view, year, month, dayOfMonth) -> {
                        this.year = year;
                        this.month = month;
                        this.day = dayOfMonth;
                        tv_date.setText(initDate(year, month + 1, dayOfMonth));
                    }
                            , year == 0 ? calendar.get(Calendar.YEAR) : year
                            , month == 0 ? calendar.get(Calendar.MONTH) : month
                            , day == 0 ? calendar.get(Calendar.DAY_OF_MONTH) : day);
            DatePicker datePicker = datePickerDialog.getDatePicker();
            datePicker.setMinDate(System.currentTimeMillis());
            datePickerDialog.show();
        });

        tv_time_start.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    this, (view, hourOfDay, minute) -> {
                s_lastHour = hourOfDay;
                s_lastMinute = minute;
                tv_time_start.setText(initTime(hourOfDay, minute));
            }, (s_lastHour == -1 ? calendar.get(Calendar.HOUR_OF_DAY) : s_lastHour)
                    , (s_lastMinute == -1 ? calendar.get(Calendar.MINUTE) : s_lastMinute)
                    , true
            );
            timePickerDialog.show();
        });

        tv_time_end.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    this, (view, hourOfDay, minute) -> {
                e_lastHour = hourOfDay;
                e_lastMinute = minute;
                tv_time_end.setText(initTime(hourOfDay, minute));
            }, (e_lastHour == -1 ? calendar.get(Calendar.HOUR_OF_DAY) : e_lastHour)
                    , (e_lastMinute == -1 ? calendar.get(Calendar.MINUTE) : e_lastMinute)
                    , true
            );
            timePickerDialog.show();
        });

        btn_confirm.setOnClickListener(v -> {
            if (isTimeValid()) {
                // Setup data response
                if (mFamiliarPlace == null) mFamiliarPlace = new FamiliarPlace();
                mFamiliarPlace.setPlace_name(tv_place.getText().toString());
                mFamiliarPlace.setLatitude(lastLatLng.latitude);
                mFamiliarPlace.setLongitude(lastLatLng.longitude);
                mFamiliarPlace.setRadius(radius);
                FPDate fpDate = mFamiliarPlace.getFp_date();
                fpDate.setDate(tv_date.getText().toString());
                fpDate.setTime_start(tv_time_start.getText().toString());
                fpDate.setTime_end(tv_time_end.getText().toString());

                Intent intent = new Intent();
                intent.putExtra(FamiliarPlaceActivity.KEY_DATA, mFamiliarPlace);
                if (index != -1) {
                    intent.putExtra(FamiliarPlaceActivity.KEY_INDEX, index);
                }
                setResult(Activity.RESULT_OK, intent);
                finish();
            } else {
                Toast.makeText(activity, "Thời gian không hợp lệ!", Toast.LENGTH_LONG).show();
            }
        });

        sl_radius.addOnChangeListener((slider, value, fromUser) -> {
            radius = (int) value + DEFAULT_RADIUS;
            drawCircle(lastLatLng);
            tv_radius.setText(initRadius(radius));
        });

    }

    private boolean isTimeValid() {
        if (s_lastHour == e_lastHour) {
            return s_lastMinute < e_lastMinute;
        } else return s_lastHour < e_lastHour;
    }

    private String initDate(int year, int month, int day) {
        return year + "-" + (month < 10 ? "0" + month : month) + "-" + (day < 10 ? "0" + day : day);
    }

    private String initTime(int hour, int second) {
        return (hour < 10 ? "0" + hour : hour) + ":" + (second < 10 ? "0" + second : second);
    }

    private String initRadius(int radius) {
        return radius + " (m)";
    }

    private void initBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(rl_bt_s);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    public void back(View view) {
        finish();
    }

    public void onHideBottomSheet(View view) {
        int state = bottomSheetBehavior.getState();
        if (state == BottomSheetBehavior.STATE_COLLAPSED) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        } else {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
    }

    private Marker lastMarker;
    private Circle lastCircle;
    private LatLng lastLatLng;

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        if (mFamiliarPlace == null) {
            lastLatLng = familiarPlaceDAO.getCurrentLocation();
            if (lastLatLng != null) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLatLng, 16));
                if (drawMarker(lastLatLng)) {
                    if (drawMarker(lastLatLng)) {
                        drawCircle(lastLatLng);
                        setPlaceText(lastLatLng);
                    }
                }
            }
        } else {
            lastLatLng = new LatLng(mFamiliarPlace.getLatitude(), mFamiliarPlace.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLatLng, 16));
            if (drawMarker(lastLatLng)) {
                sl_radius.setValue(mFamiliarPlace.getRadius() - DEFAULT_RADIUS);
            }
        }
        mMap.setOnMapClickListener(latLng -> {
            if (drawMarker(latLng)) {
                drawCircle(latLng);
                setPlaceText(latLng);
            }
        });
    }

    private void setPlaceText(LatLng latLng) {
        String place = UtilsClazz.getAddressFromLatLng(geocoder, latLng);
        tv_place.setText(place);
    }

    private boolean drawMarker(LatLng latLng) {
        if (lastMarker != null) lastMarker.remove();
        lastMarker = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .icon(ViewUtils.bitmapDescriptorFromVector(activity, R.drawable.ic_location_user)));
        if (lastMarker != null) {
            lastLatLng = latLng;
            return true;
        }
        return false;
    }

    private void drawCircle(LatLng latLng) {
        if (lastCircle != null) lastCircle.remove();
        lastCircle = mMap.addCircle(new CircleOptions()
                .center(latLng)
                .radius(radius)
                .strokeWidth(3)
                .strokeColor(Color.RED));
    }
}