package com.example.skillhive.network;

import com.example.skillhive.model.Order;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface OrdersApi {

    @POST("api/orders/service/{serviceId}")
    Call<ResponseBody> createOrder(
            @Path("serviceId") Long serviceId,
            @Query("projectTitle") String projectTitle,
            @Query("requirements") String requirements,
            @Query("deadline") String deadline,
            @Header("Authorization") String token
    );

    @GET("api/orders/my")
    Call<List<Order>> getMyOrders(
            @Header("Authorization") String token
    );

    @GET("api/orders/received")
    Call<List<Order>> getReceivedOrders(
            @Header("Authorization") String token
    );

    @PUT("api/orders/{id}/status")
    Call<ResponseBody> updateOrderStatus(
            @Path("id") Long orderId,
            @Query("status") String status,
            @Query("projectMessage") String projectMessage,
            @Query("projectLink") String projectLink,
            @Header("Authorization") String token
    );

    @PUT("api/orders/{id}/revision")
    Call<ResponseBody> requestRevision(
            @Path("id") Long orderId,
            @Query("revisionMessage") String revisionMessage,
            @Header("Authorization") String token
    );

    @PUT("api/orders/{id}/complete")
    Call<ResponseBody> completeOrder(
            @Path("id") Long orderId,
            @Header("Authorization") String token
    );

    @GET("api/orders/{id}")
    Call<Order> getOrderById(
            @Path("id") Long orderId,
            @Header("Authorization") String token
    );
}