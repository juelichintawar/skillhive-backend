package com.example.skillhive;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.skillhive.model.Service;
import com.example.skillhive.network.RetrofitClient;
import com.example.skillhive.network.ServicesApi;
import com.example.skillhive.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditServiceActivity extends AppCompatActivity {

    private EditText etTitle;
    private EditText etDescription;
    private EditText etCategory;
    private EditText etPrice;
    private EditText etImageUrl;

    private ProgressBar progressBar;
    private Button btnUpdateService;

    private SessionManager sessionManager;

    private Long serviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit_service);

        etTitle = findViewById(R.id.etEditServiceTitle);
        etDescription = findViewById(R.id.etEditServiceDescription);
        etCategory = findViewById(R.id.etEditServiceCategory);
        etPrice = findViewById(R.id.etEditServicePrice);
        etImageUrl = findViewById(R.id.etEditServiceImageUrl);

        progressBar =
                findViewById(R.id.progressBarEditService);

        btnUpdateService =
                findViewById(R.id.btnUpdateService);

        sessionManager =
                new SessionManager(this);

        serviceId =
                getIntent().getLongExtra(
                        "serviceId",
                        -1
                );

        if (serviceId == -1) {

            Toast.makeText(
                    this,
                    "Invalid service",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        loadService();

        btnUpdateService.setOnClickListener(
                v -> updateService()
        );
    }

    private void loadService() {

        progressBar.setVisibility(View.VISIBLE);

        ServicesApi api =
                RetrofitClient.getInstance()
                        .create(ServicesApi.class);

        api.getServiceById(serviceId)
                .enqueue(new Callback<Service>() {

                    @Override
                    public void onResponse(
                            Call<Service> call,
                            Response<Service> response) {

                        progressBar.setVisibility(
                                View.GONE
                        );

                        if (response.isSuccessful()
                                && response.body() != null) {

                            Service service =
                                    response.body();

                            etTitle.setText(
                                    service.getTitle()
                            );

                            etDescription.setText(
                                    service.getDescription()
                            );

                            etCategory.setText(
                                    service.getCategory()
                            );

                            etPrice.setText(
                                    String.valueOf(
                                            service.getPrice()
                                    )
                            );

                            if (service.getImageUrl() != null) {

                                etImageUrl.setText(
                                        service.getImageUrl()
                                );
                            }

                        } else {

                            Toast.makeText(
                                    EditServiceActivity.this,
                                    "Failed to load service",
                                    Toast.LENGTH_SHORT
                            ).show();

                            finish();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<Service> call,
                            Throwable t) {

                        progressBar.setVisibility(
                                View.GONE
                        );

                        Toast.makeText(
                                EditServiceActivity.this,
                                "Network error: "
                                        + t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();

                        finish();
                    }
                });
    }

    private void updateService() {

        String title =
                etTitle.getText()
                        .toString()
                        .trim();

        String description =
                etDescription.getText()
                        .toString()
                        .trim();

        String category =
                etCategory.getText()
                        .toString()
                        .trim();

        String priceText =
                etPrice.getText()
                        .toString()
                        .trim();

        String imageUrl =
                etImageUrl.getText()
                        .toString()
                        .trim();

        if (title.isEmpty()) {

            etTitle.setError(
                    "Enter service title"
            );

            etTitle.requestFocus();

            return;
        }

        if (description.isEmpty()) {

            etDescription.setError(
                    "Enter service description"
            );

            etDescription.requestFocus();

            return;
        }

        if (category.isEmpty()) {

            etCategory.setError(
                    "Enter service category"
            );

            etCategory.requestFocus();

            return;
        }

        if (priceText.isEmpty()) {

            etPrice.setError(
                    "Enter service price"
            );

            etPrice.requestFocus();

            return;
        }

        double price;

        try {

            price =
                    Double.parseDouble(priceText);

        } catch (NumberFormatException e) {

            etPrice.setError(
                    "Enter a valid price"
            );

            etPrice.requestFocus();

            return;
        }

        String token =
                sessionManager.getToken();

        if (token == null || token.isEmpty()) {

            Toast.makeText(
                    this,
                    "Please login again",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        Service updatedService =
                new Service();

        updatedService.setTitle(title);
        updatedService.setDescription(description);
        updatedService.setCategory(category);
        updatedService.setPrice(price);
        updatedService.setImageUrl(
                imageUrl.isEmpty()
                        ? null
                        : imageUrl
        );

        progressBar.setVisibility(
                View.VISIBLE
        );

        btnUpdateService.setEnabled(false);

        ServicesApi api =
                RetrofitClient.getInstance()
                        .create(ServicesApi.class);

        api.updateService(
                serviceId,
                updatedService,
                "Bearer " + token
        ).enqueue(new Callback<Service>() {

            @Override
            public void onResponse(
                    Call<Service> call,
                    Response<Service> response) {

                progressBar.setVisibility(
                        View.GONE
                );

                btnUpdateService.setEnabled(
                        true
                );

                if (response.isSuccessful()
                        && response.body() != null) {

                    Toast.makeText(
                            EditServiceActivity.this,
                            "Service updated successfully",
                            Toast.LENGTH_SHORT
                    ).show();

                    finish();

                } else {

                    Toast.makeText(
                            EditServiceActivity.this,
                            "Failed to update service",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }

            @Override
            public void onFailure(
                    Call<Service> call,
                    Throwable t) {

                progressBar.setVisibility(
                        View.GONE
                );

                btnUpdateService.setEnabled(
                        true
                );

                Toast.makeText(
                        EditServiceActivity.this,
                        "Network error: "
                                + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }
}