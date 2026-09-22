package com.example.skillhive.network;

import com.example.skillhive.model.Profile;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PUT;

public interface ProfileApi {

    @GET("api/users/profile")
    Call<Profile> getProfile(
            @Header("Authorization") String token
    );

    @PUT("api/users/profile")
    Call<Profile> updateProfile(
            @Body Profile profile,
            @Header("Authorization") String token
    );
}