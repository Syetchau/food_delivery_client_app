package com.example.liew.idelivery.Remote;

import com.example.liew.idelivery.Model.MyResponse;
import com.example.liew.idelivery.Model.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAA-I4Eb74:APA91bHhEpGVC-ASTX8K2KHd4L3S3q4Yv_JTqapb6XHwX5rE8zo9F12FQM8R8cl5VgFu-G2_zDK4gAonoPDPb2yMGpUkui43HatR9qggJua5hq_EmNOmAJtB0a3WfJNUX8WawQqFFn6L"
            }

    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
