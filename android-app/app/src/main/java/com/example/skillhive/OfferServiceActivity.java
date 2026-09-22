package com.example.skillhive;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.skillhive.model.Service;
import com.example.skillhive.network.CreateServiceApi;
import com.example.skillhive.network.RetrofitClient;
import com.example.skillhive.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OfferServiceActivity extends AppCompatActivity {

    private EditText etServiceTitle;
    private EditText etServiceDescription;
    private EditText etServiceCategory;
    private EditText etServicePrice;
    private EditText etServiceImageUrl;
    private ProgressBar progressBar;
    private Button btnCreateService;

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offer_service);

        etServiceTitle = findViewById(R.id.etServiceTitle);
        etServiceDescription = findViewById(R.id.etServiceDescription);
        etServiceCategory = findViewById(R.id.etServiceCategory);
        etServicePrice = findViewById(R.id.etServicePrice);
        etServiceImageUrl = findViewById(R.id.etServiceImageUrl);
        progressBar = findViewById(R.id.progressBarOfferService);
        btnCreateService = findViewById(R.id.btnCreateService);

        sessionManager = new SessionManager(this);

        btnCreateService.setOnClickListener(v -> createService());
    }

    private void createService() {

        String title = etServiceTitle.getText().toString().trim();
        String description = etServiceDescription.getText().toString().trim();
        String category = etServiceCategory.getText().toString().trim();
        String priceText = etServicePrice.getText().toString().trim();
        String imageUrl = etServiceImageUrl.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            etServiceTitle.setError("Enter service title");
            etServiceTitle.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(description)) {
            etServiceDescription.setError("Enter service description");
            etServiceDescription.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(category)) {
            etServiceCategory.setError("Enter service category");
            etServiceCategory.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(priceText)) {
            etServicePrice.setError("Enter service price");
            etServicePrice.requestFocus();
            return;
        }

        double price;

        try {
            price = Double.parseDouble(priceText);
        } catch (NumberFormatException e) {
            etServicePrice.setError("Enter a valid price");
            etServicePrice.requestFocus();
            return;
        }

        if (price <= 0) {
            etServicePrice.setError("Price must be greater than 0");
            etServicePrice.requestFocus();
            return;
        }

        Service service = new Service();

        service.setTitle(title);
        service.setDescription(description);
        service.setCategory(category);
        service.setPrice(price);

        if (!TextUtils.isEmpty(imageUrl)) {
            service.setImageUrl(imageUrl);
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

        progressBar.setVisibility(View.VISIBLE);
        btnCreateService.setEnabled(false);

        CreateServiceApi api =
                RetrofitClient.getInstance()
                        .create(CreateServiceApi.class);

        api.createService(
                service,
                "Bearer " + token
        ).enqueue(new Callback<Service>() {

            @Override
            public void onResponse(
                    Call<Service> call,
                    Response<Service> response) {

                progressBar.setVisibility(View.GONE);
                btnCreateService.setEnabled(true);

                if (response.isSuccessful()) {

                    Toast.makeText(
                            OfferServiceActivity.this,
                            "Service published successfully",
                            Toast.LENGTH_SHORT
                    ).show();

                    finish();

                } else {

                    Toast.makeText(
                            OfferServiceActivity.this,
                            "Failed to publish service",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }

            @Override
            public void onFailure(
                    Call<Service> call,
                    Throwable t) {

                progressBar.setVisibility(View.GONE);
                btnCreateService.setEnabled(true);

                Toast.makeText(
                        OfferServiceActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }
}