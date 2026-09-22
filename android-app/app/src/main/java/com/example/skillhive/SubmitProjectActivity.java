package com.example.skillhive;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.skillhive.network.OrdersApi;
import com.example.skillhive.network.RetrofitClient;
import com.example.skillhive.utils.SessionManager;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SubmitProjectActivity extends AppCompatActivity {

    private EditText etProjectMessage;
    private EditText etProjectLink;
    private AppCompatButton btnSubmitProject;

    private OrdersApi ordersApi;
    private SessionManager sessionManager;

    private Long orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_submit_project);

        // Get order ID
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

        // Initialize views
        etProjectMessage = findViewById(R.id.etProjectMessage);
        etProjectLink = findViewById(R.id.etProjectLink);
        btnSubmitProject = findViewById(R.id.btnSubmitProject);

        // Initialize API
        ordersApi = RetrofitClient
                .getInstance()
                .create(OrdersApi.class);

        // Initialize session
        sessionManager = new SessionManager(this);

        // Submit button
        btnSubmitProject.setOnClickListener(
                v -> submitProject()
        );
    }

    private void submitProject() {

        String message = etProjectMessage
                .getText()
                .toString()
                .trim();

        String projectLink = etProjectLink
                .getText()
                .toString()
                .trim();

        // Validate project message
        if (TextUtils.isEmpty(message)) {

            etProjectMessage.setError(
                    "Please enter a project message"
            );

            etProjectMessage.requestFocus();
            return;
        }

        // Validate project link
        if (TextUtils.isEmpty(projectLink)) {

            etProjectLink.setError(
                    "Please enter your project link"
            );

            etProjectLink.requestFocus();
            return;
        }

        // Get authentication token
        String token = sessionManager.getToken();

        if (token == null || token.isEmpty()) {

            Toast.makeText(
                    this,
                    "Please login again",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // Disable button
        btnSubmitProject.setEnabled(false);
        btnSubmitProject.setText("Submitting...");

        // Send request to backend
        ordersApi.updateOrderStatus(
                orderId,
                "SUBMITTED",
                message,
                projectLink,
                "Bearer " + token
        ).enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(
                    Call<ResponseBody> call,
                    Response<ResponseBody> response) {

                if (response.isSuccessful()) {

                    Toast.makeText(
                            SubmitProjectActivity.this,
                            "Project submitted successfully!",
                            Toast.LENGTH_LONG
                    ).show();

                    finish();

                } else {

                    String errorMessage =
                            "Unknown server error";

                    try {
                        if (response.errorBody() != null) {
                            errorMessage =
                                    response.errorBody().string();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    btnSubmitProject.setEnabled(true);
                    btnSubmitProject.setText(
                            "Submit Project"
                    );

                    Toast.makeText(
                            SubmitProjectActivity.this,
                            "Server Error "
                                    + response.code()
                                    + ": "
                                    + errorMessage,
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onFailure(
                    Call<ResponseBody> call,
                    Throwable t) {

                btnSubmitProject.setEnabled(true);
                btnSubmitProject.setText(
                        "Submit Project"
                );

                Toast.makeText(
                        SubmitProjectActivity.this,
                        "Connection failed: "
                                + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }
}