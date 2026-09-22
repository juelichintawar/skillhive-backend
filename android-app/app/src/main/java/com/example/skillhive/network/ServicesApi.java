package com.example.skillhive.network;

import com.example.skillhive.model.Service;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ServicesApi {

    @GET("api/services")
    Call<List<Service>> getAllServices();

    @GET("api/services/my")
    Call<List<Service>> getMyServices(
            @Header("Authorization") String token
    );

    @GET("api/services/{id}")
    Call<Service> getServiceById(
            @Path("id") Long serviceId
    );

    @PUT("api/services/{id}")
    Call<Service> updateService(
            @Path("id") Long serviceId,
            @Body Service service,
            @Header("Authorization") String token
    );

    @DELETE("api/services/{id}")
    Call<okhttp3.ResponseBody> deleteService(
            @Path("id") Long serviceId,
            @Header("Authorization") String token
    );
}