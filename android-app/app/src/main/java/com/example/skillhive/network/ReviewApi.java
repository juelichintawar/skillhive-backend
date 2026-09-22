package com.example.skillhive.network;

import com.example.skillhive.model.Review;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ReviewApi {

    @POST("api/reviews/order/{orderId}")
    Call<Review> createReview(
            @Path("orderId") Long orderId,
            @Body Review review,
            @Header("Authorization") String token
    );

    @GET("api/reviews/provider/{providerId}")
    Call<List<Review>> getProviderReviews(
            @Path("providerId") Long providerId
    );

    @GET("api/reviews/order/{orderId}")
    Call<Review> getOrderReview(
            @Path("orderId") Long orderId
    );
}