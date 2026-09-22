package com.example.skillhive.network;

import com.example.skillhive.model.Notification;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface NotificationApi {

    @GET("api/notifications")
    Call<List<Notification>> getMyNotifications(
            @Header("Authorization") String token
    );

    @GET("api/notifications/unread")
    Call<List<Notification>> getUnreadNotifications(
            @Header("Authorization") String token
    );

    @GET("api/notifications/unread/count")
    Call<Long> getUnreadCount(
            @Header("Authorization") String token
    );

    @PUT("api/notifications/{id}/read")
    Call<Notification> markAsRead(
            @Path("id") Long notificationId,
            @Header("Authorization") String token
    );
}