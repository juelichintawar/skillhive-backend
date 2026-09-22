package com.example.skillhive.network;

import com.example.skillhive.model.WalletTransaction;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface WalletTransactionApi {

    @GET("api/wallet/transactions")
    Call<List<WalletTransaction>> getMyTransactions(
            @Header("Authorization") String token
    );
}