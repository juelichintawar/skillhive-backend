package com.example.skillhive.network;

import com.example.skillhive.model.Payment;

import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface PaymentApi {

    @POST("api/payments/create")
    Call<Payment> createPayment(
            @Query("amount") Double amount,
            @Header("Authorization") String token
    );

    @POST("api/payments/{paymentId}/confirm")
    Call<Payment> confirmPayment(
            @Path("paymentId") Long paymentId,
            @Header("Authorization") String token
    );
}