package com.example.skillhive.network;

import com.example.skillhive.model.Favorite;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface FavoritesApi {

    @POST("api/favorites/service/{serviceId}")
    Call<Favorite> addFavorite(
            @Path("serviceId") Long serviceId,
            @Header("Authorization") String token
    );

    @GET("api/favorites/my")
    Call<List<Favorite>> getMyFavorites(
            @Header("Authorization") String token
    );

    @DELETE("api/favorites/service/{serviceId}")
    Call<Void> removeFavorite(
            @Path("serviceId") Long serviceId,
            @Header("Authorization") String token
    );
}