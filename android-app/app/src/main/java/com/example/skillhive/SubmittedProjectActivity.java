package com.example.skillhive;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.skillhive.model.Order;
import com.example.skillhive.network.OrdersApi;
import com.example.skillhive.network.RetrofitClient;
import com.example.skillhive.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SubmittedProjectActivity extends AppCompatActivity {

    private EditText etProjectMessage;
    private EditText etProjectLink;

    private AppCompatButton btnRequestChanges;
    private AppCompatButton btnAcceptComplete;

    private OrdersApi ordersApi;
    private SessionManager sessionManager;

    private Long orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_submitted_project);

        orderId = getIntent().getLongExtra("orderId", -1);

        if (orderId == -1) {

            Toast.makeText(
                    this,
                    "Invalid order",
                    Toast.LENGTH_LONG
            ).show();

            finish();
            return;
        }

        etProjectMessage =
                findViewById(R.id.etProjectMessage);

        etProjectLink =
                findViewById(R.id.etProjectLink);

        btnRequestChanges =
                findViewById(R.id.btnRequestChanges);

        btnAcceptComplete =
                findViewById(R.id.btnAcceptComplete);

        ordersApi =
                RetrofitClient.getInstance()
                        .create(OrdersApi.class);

        sessionManager =
                new SessionManager(this);

        btnRequestChanges.setOnClickListener(
                v -> openRequestChangesScreen()
        );

        btnAcceptComplete.setOnClickListener(
                v -> acceptAndComplete()
        );

        loadSubmittedProject();
    }

    private void loadSubmittedProject() {

        String token = sessionManager.getToken();

        if (token == null || token.isEmpty()) {

            Toast.makeText(
                    this,
                    "Please login again",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        ordersApi.getOrderById(
                orderId,
                "Bearer " + token
        ).enqueue(new Callback<Order>() {

            @Override
            public void onResponse(
                    Call<Order> call,
                    Response<Order> response) {

                if (response.isSuccessful()
                        && response.body() != null) {

                    Order order = response.body();

                    if (order.getProjectMessage() != null) {

                        etProjectMessage.setText(
                                order.getProjectMessage()
                        );
                    }

                    if (order.getProjectLink() != null) {

                        etProjectLink.setText(
                                order.getProjectLink()
                        );
                    }

                } else {

                    Toast.makeText(
                            SubmittedProjectActivity.this,
                            "Failed to load submitted project",
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onFailure(
                    Call<Order> call,
                    Throwable t) {

                Toast.makeText(
                        SubmittedProjectActivity.this,
                        "Connection failed: "
                                + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void openRequestChangesScreen() {

        Intent intent =
                new Intent(
                        SubmittedProjectActivity.this,
                        RequestChangesActivity.class
                );

        intent.putExtra(
                "orderId",
                orderId
        );

        startActivity(intent);
    }

    private void acceptAndComplete() {

        String token = sessionManager.getToken();

        if (token == null || token.isEmpty()) {

            Toast.makeText(
                    this,
                    "Please login again",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        btnAcceptComplete.setEnabled(false);
        btnAcceptComplete.setText("Completing...");

        ordersApi.completeOrder(
                orderId,
                "Bearer " + token
        ).enqueue(new Callback<okhttp3.ResponseBody>() {

            @Override
            public void onResponse(
                    Call<okhttp3.ResponseBody> call,
                    Response<okhttp3.ResponseBody> response) {

                btnAcceptComplete.setEnabled(true);
                btnAcceptComplete.setText(
                        "Accept & Complete"
                );

                if (response.isSuccessful()) {

                    Toast.makeText(
                            SubmittedProjectActivity.this,
                            "Project accepted and completed!",
                            Toast.LENGTH_LONG
                    ).show();

                    finish();

                } else {

                    String errorMessage =
                            "Server error " + response.code();

                    try {

                        if (response.errorBody() != null) {

                            errorMessage =
                                    response.errorBody().string();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Toast.makeText(
                            SubmittedProjectActivity.this,
                            errorMessage,
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onFailure(
                    Call<okhttp3.ResponseBody> call,
                    Throwable t) {

                btnAcceptComplete.setEnabled(true);
                btnAcceptComplete.setText(
                        "Accept & Complete"
                );

                Toast.makeText(
                        SubmittedProjectActivity.this,
                        "Connection failed: "
                                + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }
}