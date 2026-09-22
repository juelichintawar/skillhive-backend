package com.example.skillhive;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.skillhive.model.WalletTransaction;
import com.example.skillhive.network.RetrofitClient;
import com.example.skillhive.network.WalletTransactionApi;
import com.example.skillhive.utils.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WalletTransactionsActivity extends AppCompatActivity {

    private RecyclerView recyclerTransactions;
    private ProgressBar progressBarTransactions;
    private TextView tvNoTransactions;

    private WalletTransactionApi transactionApi;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_wallet_transactions);

        recyclerTransactions =
                findViewById(R.id.recyclerTransactions);

        progressBarTransactions =
                findViewById(R.id.progressBarTransactions);

        tvNoTransactions =
                findViewById(R.id.tvNoTransactions);

        recyclerTransactions.setLayoutManager(
                new LinearLayoutManager(this)
        );

        transactionApi = RetrofitClient
                .getInstance()
                .create(WalletTransactionApi.class);

        sessionManager = new SessionManager(this);

        loadTransactions();
    }

    private void loadTransactions() {

        String token = sessionManager.getToken();

        if (token == null || token.isEmpty()) {

            Toast.makeText(
                    this,
                    "Please login again",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        progressBarTransactions.setVisibility(
                View.VISIBLE
        );

        tvNoTransactions.setVisibility(
                View.GONE
        );

        transactionApi.getMyTransactions(
                "Bearer " + token
        ).enqueue(new Callback<List<WalletTransaction>>() {

            @Override
            public void onResponse(
                    Call<List<WalletTransaction>> call,
                    Response<List<WalletTransaction>> response) {

                progressBarTransactions.setVisibility(
                        View.GONE
                );

                if (response.isSuccessful()
                        && response.body() != null) {

                    List<WalletTransaction> transactions =
                            response.body();

                    if (transactions.isEmpty()) {

                        tvNoTransactions.setVisibility(
                                View.VISIBLE
                        );

                        recyclerTransactions.setVisibility(
                                View.GONE
                        );

                    } else {

                        tvNoTransactions.setVisibility(
                                View.GONE
                        );

                        recyclerTransactions.setVisibility(
                                View.VISIBLE
                        );

                        WalletTransactionAdapter adapter =
                                new WalletTransactionAdapter(
                                        transactions
                                );

                        recyclerTransactions.setAdapter(
                                adapter
                        );
                    }

                } else {

                    Toast.makeText(
                            WalletTransactionsActivity.this,
                            "Failed to load transactions: "
                                    + response.code(),
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onFailure(
                    Call<List<WalletTransaction>> call,
                    Throwable t) {

                progressBarTransactions.setVisibility(
                        View.GONE
                );

                Toast.makeText(
                        WalletTransactionsActivity.this,
                        "Connection failed: "
                                + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    @Override
    protected void onResume() {

        super.onResume();

        loadTransactions();
    }
}