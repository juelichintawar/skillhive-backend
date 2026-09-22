package com.example.skillhive.network;

import com.example.skillhive.model.LoginRequest;
import com.example.skillhive.model.LoginResponse;
import com.example.skillhive.model.RegisterRequest;
import com.example.skillhive.model.RegisterResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApi {

    @POST("api/auth/register")
    Call<RegisterResponse> register(@Body RegisterRequest request);

    @POST("api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);
}