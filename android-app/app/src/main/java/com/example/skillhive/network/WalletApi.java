package com.example.skillhive.network;

import com.example.skillhive.model.Wallet;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface WalletApi {

    @GET("api/wallet")
    Call<Wallet> getMyWallet(
            @Header("Authorization") String token
    );

    @POST("api/wallet/add")
    Call<Wallet> addMoney(
            @Query("amount") Double amount,
            @Header("Authorization") String token
    );
}