package com.example.skillhive.network;

import com.example.skillhive.model.Service;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface CreateServiceApi {

    @POST("api/services")
    Call<Service> createService(
            @Body Service service,
            @Header("Authorization") String token
    );
}