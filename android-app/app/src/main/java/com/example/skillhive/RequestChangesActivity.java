package com.example.skillhive;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.skillhive.model.Order;
import com.example.skillhive.network.OrdersApi;
import com.example.skillhive.network.RetrofitClient;
import com.example.skillhive.utils.SessionManager;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RequestChangesActivity extends AppCompatActivity {

    private TextView tvProjectTitle;
    private TextView tvServiceName;
    private TextView tvPreviousMessage;
    private TextView tvPreviousLink;
    private TextView tvCharacterCount;

    private EditText etRevisionMessage;

    private AppCompatButton btnSendRevision;

    private OrdersApi ordersApi;
    private SessionManager sessionManager;

    private Long orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_request_changes);

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

        tvProjectTitle = findViewById(R.id.tvProjectTitle);
        tvServiceName = findViewById(R.id.tvServiceName);
        tvPreviousMessage = findViewById(R.id.tvPreviousMessage);
        tvPreviousLink = findViewById(R.id.tvPreviousLink);
        tvCharacterCount = findViewById(R.id.tvCharacterCount);

        etRevisionMessage =
                findViewById(R.id.etRevisionMessage);

        btnSendRevision =
                findViewById(R.id.btnSendRevision);

        ordersApi =
                RetrofitClient.getInstance()
                        .create(OrdersApi.class);

        sessionManager =
                new SessionManager(this);

        setupCharacterCounter();

        btnSendRevision.setOnClickListener(
                v -> sendRevisionRequest()
        );

        loadOrder();
    }

    private void setupCharacterCounter() {

        etRevisionMessage.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count) {

                        tvCharacterCount.setText(
                                s.length() + " / 3000"
                        );
                    }

                    @Override
                    public void afterTextChanged(
                            Editable s) {
                    }
                }
        );
    }

    private void loadOrder() {

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

                    displayOrder(order);

                } else {

                    Toast.makeText(
                            RequestChangesActivity.this,
                            "Failed to load project details",
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onFailure(
                    Call<Order> call,
                    Throwable t) {

                Toast.makeText(
                        RequestChangesActivity.this,
                        "Connection failed: "
                                + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void displayOrder(Order order) {

        if (order.getProjectTitle() != null
                && !order.getProjectTitle().isEmpty()) {

            tvProjectTitle.setText(
                    order.getProjectTitle()
            );
        } else {

            tvProjectTitle.setText(
                    "Project"
            );
        }

        if (order.getService() != null
                && order.getService().getTitle() != null) {

            tvServiceName.setText(
                    order.getService().getTitle()
            );
        } else {

            tvServiceName.setText(
                    "Service"
            );
        }

        if (order.getProjectMessage() != null
                && !order.getProjectMessage().isEmpty()) {

            tvPreviousMessage.setText(
                    order.getProjectMessage()
            );
        } else {

            tvPreviousMessage.setText(
                    "No previous submission message."
            );
        }

        if (order.getProjectLink() != null
                && !order.getProjectLink().isEmpty()) {

            tvPreviousLink.setText(
                    order.getProjectLink()
            );
        } else {

            tvPreviousLink.setText(
                    "No project link available."
            );
        }
    }

    private void sendRevisionRequest() {

        String revisionMessage =
                etRevisionMessage
                        .getText()
                        .toString()
                        .trim();

        if (TextUtils.isEmpty(revisionMessage)) {

            etRevisionMessage.setError(
                    "Please describe the changes you want"
            );

            etRevisionMessage.requestFocus();

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

        btnSendRevision.setEnabled(false);
        btnSendRevision.setText("Sending...");

        ordersApi.requestRevision(
                orderId,
                revisionMessage,
                "Bearer " + token
        ).enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(
                    Call<ResponseBody> call,
                    Response<ResponseBody> response) {

                if (response.isSuccessful()) {

                    Toast.makeText(
                            RequestChangesActivity.this,
                            "Changes request sent to provider",
                            Toast.LENGTH_LONG
                    ).show();

                    finish();

                } else {

                    btnSendRevision.setEnabled(true);
                    btnSendRevision.setText(
                            "Send Request to Provider"
                    );

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
                            RequestChangesActivity.this,
                            errorMessage,
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onFailure(
                    Call<ResponseBody> call,
                    Throwable t) {

                btnSendRevision.setEnabled(true);
                btnSendRevision.setText(
                        "Send Request to Provider"
                );

                Toast.makeText(
                        RequestChangesActivity.this,
                        "Connection failed: "
                                + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }
}