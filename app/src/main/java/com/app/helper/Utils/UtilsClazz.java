package com.app.helper.Utils;

import android.location.Address;
import android.location.Geocoder;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class UtilsClazz {
    public static String random6Code() {
        return String.valueOf(new Random().nextInt(999999 - 100000) + 100000);
    }

    public static String random8Code() {
        return String.valueOf(new Random().nextInt(99999999 - 10000000) + 10000000);
    }

    public static String getCurrentDateTimeFormat() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public static String convertTimeMillisToDate(long timeMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeMillis);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH) + 1; // 0 - 11
        int year = calendar.get(Calendar.YEAR);
        return year + "-" + (month < 10 ? "0" + month : month) + "-" + (day < 10 ? "0" + day : day);
    }

    public static String getAddressFromLatLng(Geocoder geocoder, LatLng latLng) {
        try {
            Address address = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                    .get(0);
            String addressLine = address.getAddressLine(0);
            addressLine = addressLine.substring(0, addressLine.lastIndexOf(","));
            return addressLine;
        } catch (IOException e) {
            return "Không tìm thấy vị trí";
        }
    }

}
