package com.example.sean.bei_na_song_liechat.Fragments;

import com.example.sean.bei_na_song_liechat.Notification.MyResponse;
import com.example.sean.bei_na_song_liechat.Notification.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAdNN3zQA:APA91bE8ei7w6omztgmRRxBAjYcRgPeF2V3OyTuEKD8TtBdKlXkpUaSXd14dxn3LckRpF44aULl-GgVVLkJCCyYdXmRJsuY-kjZwEogJWlBvU_7VQawSIIodJx5MAM7R9Gx1r8lUkVqZ"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);

}
