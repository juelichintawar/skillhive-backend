package com.example.skillhive;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.skillhive.model.Payment;
import com.example.skillhive.model.Wallet;
import com.example.skillhive.network.PaymentApi;
import com.example.skillhive.network.RetrofitClient;
import com.example.skillhive.network.WalletApi;
import com.example.skillhive.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WalletActivity extends AppCompatActivity {

    private TextView tvBalance;
    private EditText etAmount;

    private AppCompatButton btnAddMoney;
    private AppCompatButton btnAmount100;
    private AppCompatButton btnAmount500;
    private AppCompatButton btnAmount1000;
    private AppCompatButton btnTransactionHistory;

    private ProgressBar progressBarWallet;

    private WalletApi walletApi;
    private PaymentApi paymentApi;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_wallet);

        // Views
        tvBalance = findViewById(R.id.tvBalance);
        etAmount = findViewById(R.id.etAmount);

        btnAddMoney = findViewById(R.id.btnAddMoney);

        btnAmount100 = findViewById(R.id.btnAmount100);
        btnAmount500 = findViewById(R.id.btnAmount500);
        btnAmount1000 = findViewById(R.id.btnAmount1000);

        btnTransactionHistory =
                findViewById(R.id.btnTransactionHistory);

        progressBarWallet =
                findViewById(R.id.progressBarWallet);

        // API
        walletApi = RetrofitClient
                .getInstance()
                .create(WalletApi.class);

        paymentApi = RetrofitClient
                .getInstance()
                .create(PaymentApi.class);

        // Session
        sessionManager = new SessionManager(this);

        // Load wallet
        loadWallet();

        // Add Money
        btnAddMoney.setOnClickListener(
                v -> createPayment()
        );

        // Quick amounts
        btnAmount100.setOnClickListener(
                v -> setQuickAmount("100")
        );

        btnAmount500.setOnClickListener(
                v -> setQuickAmount("500")
        );

        btnAmount1000.setOnClickListener(
                v -> setQuickAmount("1000")
        );

        // Transaction History
        btnTransactionHistory.setOnClickListener(
                v -> openTransactionHistory()
        );
    }

    // ---------------------------------------------------------
    // TRANSACTION HISTORY
    // ---------------------------------------------------------

    private void openTransactionHistory() {

        Intent intent = new Intent(
                WalletActivity.this,
                WalletTransactionsActivity.class
        );

        startActivity(intent);
    }

    // ---------------------------------------------------------
    // QUICK AMOUNT
    // ---------------------------------------------------------

    private void setQuickAmount(String amount) {

        etAmount.setText(amount);

        etAmount.setSelection(
                etAmount.length()
        );

        etAmount.requestFocus();
    }

    // ---------------------------------------------------------
    // LOAD WALLET
    // ---------------------------------------------------------

    private void loadWallet() {

        String token = sessionManager.getToken();

        if (token == null || token.isEmpty()) {

            Toast.makeText(
                    this,
                    "Please login again",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        progressBarWallet.setVisibility(
                View.VISIBLE
        );

        walletApi.getMyWallet(
                "Bearer " + token
        ).enqueue(new Callback<Wallet>() {

            @Override
            public void onResponse(
                    Call<Wallet> call,
                    Response<Wallet> response) {

                progressBarWallet.setVisibility(
                        View.GONE
                );

                if (response.isSuccessful()
                        && response.body() != null) {

                    Wallet wallet =
                            response.body();

                    updateBalance(
                            wallet.getBalance()
                    );

                } else {

                    Toast.makeText(
                            WalletActivity.this,
                            "Failed to load wallet: "
                                    + response.code(),
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onFailure(
                    Call<Wallet> call,
                    Throwable t) {

                progressBarWallet.setVisibility(
                        View.GONE
                );

                Toast.makeText(
                        WalletActivity.this,
                        "Connection failed: "
                                + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    // ---------------------------------------------------------
    // CREATE PAYMENT
    // ---------------------------------------------------------

    private void createPayment() {

        String amountText =
                etAmount.getText().toString().trim();

        if (TextUtils.isEmpty(amountText)) {

            etAmount.setError("Enter amount");
            etAmount.requestFocus();

            return;
        }

        Double amount;

        try {

            amount = Double.parseDouble(amountText);

        } catch (NumberFormatException e) {

            etAmount.setError(
                    "Enter a valid amount"
            );

            etAmount.requestFocus();

            return;
        }

        if (amount <= 0) {

            etAmount.setError(
                    "Amount must be greater than 0"
            );

            etAmount.requestFocus();

            return;
        }

        if (amount > 10000) {

            etAmount.setError(
                    "Maximum top-up is ₹10000"
            );

            etAmount.requestFocus();

            return;
        }

        String token = sessionManager.getToken();

        if (token == null || token.isEmpty()) {

            Toast.makeText(
                    this,
                    "Please login again",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        setLoading(true);

        paymentApi.createPayment(
                amount,
                "Bearer " + token
        ).enqueue(new Callback<Payment>() {

            @Override
            public void onResponse(
                    Call<Payment> call,
                    Response<Payment> response) {

                if (response.isSuccessful()
                        && response.body() != null) {

                    Payment payment =
                            response.body();

                    if ("PENDING".equals(
                            payment.getStatus())) {

                        showTestPayment(payment);

                    } else {

                        setLoading(false);

                        Toast.makeText(
                                WalletActivity.this,
                                "Unexpected payment status",
                                Toast.LENGTH_LONG
                        ).show();
                    }

                } else {

                    setLoading(false);

                    String message =
                            "Failed to create payment: "
                                    + response.code();

                    try {

                        if (response.errorBody() != null) {

                            message =
                                    response.errorBody().string();
                        }

                    } catch (Exception e) {

                        e.printStackTrace();
                    }

                    Toast.makeText(
                            WalletActivity.this,
                            message,
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onFailure(
                    Call<Payment> call,
                    Throwable t) {

                setLoading(false);

                Toast.makeText(
                        WalletActivity.this,
                        "Connection failed: "
                                + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    // ---------------------------------------------------------
    // TEST PAYMENT
    // ---------------------------------------------------------

    private void showTestPayment(Payment payment) {

        setLoading(false);

        String amount =
                String.format(
                        "%.2f",
                        payment.getAmount()
                );

        new AlertDialog.Builder(this)

                .setTitle("Test Payment")

                .setMessage(
                        "This is a test payment.\n\n"
                                + "Amount: ₹"
                                + amount
                                + "\n\nPayment ID: "
                                + payment.getId()
                )

                .setNegativeButton(
                        "Cancel",
                        (dialog, which) ->
                                dialog.dismiss()
                )

                .setPositiveButton(
                        "Pay Now",
                        (dialog, which) ->
                                confirmPayment(
                                        payment.getId()
                                )
                )

                .setCancelable(false)

                .show();
    }

    // ---------------------------------------------------------
    // CONFIRM PAYMENT
    // ---------------------------------------------------------

    private void confirmPayment(Long paymentId) {

        String token = sessionManager.getToken();

        if (token == null || token.isEmpty()) {

            Toast.makeText(
                    this,
                    "Please login again",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        setLoading(true);

        paymentApi.confirmPayment(
                paymentId,
                "Bearer " + token
        ).enqueue(new Callback<Payment>() {

            @Override
            public void onResponse(
                    Call<Payment> call,
                    Response<Payment> response) {

                setLoading(false);

                if (response.isSuccessful()
                        && response.body() != null) {

                    Payment payment =
                            response.body();

                    if ("SUCCESS".equals(
                            payment.getStatus())) {

                        etAmount.setText("");

                        loadWallet();

                        Toast.makeText(
                                WalletActivity.this,
                                "Payment successful! "
                                        + "Money added to wallet.",
                                Toast.LENGTH_LONG
                        ).show();

                    } else {

                        Toast.makeText(
                                WalletActivity.this,
                                "Payment was not successful.",
                                Toast.LENGTH_LONG
                        ).show();
                    }

                } else {

                    String message =
                            "Payment failed: "
                                    + response.code();

                    try {

                        if (response.errorBody() != null) {

                            message =
                                    response.errorBody().string();
                        }

                    } catch (Exception e) {

                        e.printStackTrace();
                    }

                    Toast.makeText(
                            WalletActivity.this,
                            message,
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onFailure(
                    Call<Payment> call,
                    Throwable t) {

                setLoading(false);

                Toast.makeText(
                        WalletActivity.this,
                        "Connection failed: "
                                + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    // ---------------------------------------------------------
    // UPDATE BALANCE
    // ---------------------------------------------------------

    private void updateBalance(Double balance) {

        if (balance == null) {

            tvBalance.setText("₹0.00");

            return;
        }

        tvBalance.setText(
                "₹" + String.format(
                        "%.2f",
                        balance
                )
        );
    }

    // ---------------------------------------------------------
    // LOADING STATE
    // ---------------------------------------------------------

    private void setLoading(boolean loading) {

        if (loading) {

            btnAddMoney.setEnabled(false);

            btnAmount100.setEnabled(false);
            btnAmount500.setEnabled(false);
            btnAmount1000.setEnabled(false);

            btnTransactionHistory.setEnabled(false);

            btnAddMoney.setText("Processing...");

            progressBarWallet.setVisibility(
                    View.VISIBLE
            );

        } else {

            btnAddMoney.setEnabled(true);

            btnAmount100.setEnabled(true);
            btnAmount500.setEnabled(true);
            btnAmount1000.setEnabled(true);

            btnTransactionHistory.setEnabled(true);

            btnAddMoney.setText("Add Money");

            progressBarWallet.setVisibility(
                    View.GONE
            );
        }
    }

    // ---------------------------------------------------------
    // REFRESH
    // ---------------------------------------------------------

    @Override
    protected void onResume() {

        super.onResume();

        loadWallet();
    }
}