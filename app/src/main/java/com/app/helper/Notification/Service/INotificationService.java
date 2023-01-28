package com.app.helper.Notification.Service;

import com.app.helper.Location.Zoning.Model.DataRequestLocationZoning;
import com.app.helper.Notification.Model.NotificationModel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface INotificationService {
    String BASE_URL = "https://fcm.googleapis.com/fcm/";

    Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create();
    INotificationService INSTANCE_SINGLE = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(INotificationService.class);

//    INotificationService GROUP = new Retrofit.Builder()
//            .baseUrl(BASE_URL)
//            .addConverterFactory(GsonConverterFactory.create(gson))
//            .build()
//            .create(INotificationService.class);

    @Headers({
            "Authorization: key=AAAAb_WkCbk:APA91bFDU2MgzZHiB7a9srdFDBC1DQo-89N76m5wk366hutkF2nIf3eIImkfftEvdmOSuGNhFFLWS3zr18IgT4t0munoQbdKF0b1a84jM9U5Wog_YXNKE66txPSAEBYZ4kP0WTQjhTSz",
            "Content-Type: application/json"
    })
    @POST("send")
    Call<NotificationModel> sendNotification(@Body NotificationModel notificationModel);

    @Headers({
            "Authorization: key=AAAAb_WkCbk:APA91bFDU2MgzZHiB7a9srdFDBC1DQo-89N76m5wk366hutkF2nIf3eIImkfftEvdmOSuGNhFFLWS3zr18IgT4t0munoQbdKF0b1a84jM9U5Wog_YXNKE66txPSAEBYZ4kP0WTQjhTSz",
            "Content-Type: application/json"
    })
    @POST("send")
    Call<Void> sendNotification(@Body DataRequestLocationZoning dataRequestLocationZoning);

    @Headers({
            "Authorization: key=AAAAb_WkCbk:APA91bFDU2MgzZHiB7a9srdFDBC1DQo-89N76m5wk366hutkF2nIf3eIImkfftEvdmOSuGNhFFLWS3zr18IgT4t0munoQbdKF0b1a84jM9U5Wog_YXNKE66txPSAEBYZ4kP0WTQjhTSz",
            "Content-Type: application/json"
    })
    @POST("send")
    Call<Void> sendNotification(@Body Map<String, Object> dataRequestMapLocationZoning);


}
