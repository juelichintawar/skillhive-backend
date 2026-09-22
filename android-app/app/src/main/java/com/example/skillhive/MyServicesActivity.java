package com.example.skillhive;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.skillhive.model.Service;
import com.example.skillhive.network.RetrofitClient;
import com.example.skillhive.network.ServicesApi;
import com.example.skillhive.utils.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyServicesActivity extends AppCompatActivity {

    private LinearLayout myServicesContainer;
    private ProgressBar progressBar;
    private TextView tvNoMyServices;

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_services);

        myServicesContainer =
                findViewById(R.id.myServicesContainer);

        progressBar =
                findViewById(R.id.progressBarMyServices);

        tvNoMyServices =
                findViewById(R.id.tvNoMyServices);

        sessionManager =
                new SessionManager(this);

        loadMyServices();
    }

    private void loadMyServices() {

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
        myServicesContainer.removeAllViews();
        tvNoMyServices.setVisibility(View.GONE);

        ServicesApi api =
                RetrofitClient.getInstance()
                        .create(ServicesApi.class);

        api.getMyServices(
                "Bearer " + token
        ).enqueue(new Callback<List<Service>>() {

            @Override
            public void onResponse(
                    Call<List<Service>> call,
                    Response<List<Service>> response) {

                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful()
                        && response.body() != null) {

                    List<Service> services =
                            response.body();

                    if (services.isEmpty()) {

                        tvNoMyServices.setVisibility(
                                View.VISIBLE
                        );

                    } else {

                        for (Service service : services) {
                            addServiceCard(service);
                        }
                    }

                } else {

                    Toast.makeText(
                            MyServicesActivity.this,
                            "Failed to load your services",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }

            @Override
            public void onFailure(
                    Call<List<Service>> call,
                    Throwable t) {

                progressBar.setVisibility(View.GONE);

                Toast.makeText(
                        MyServicesActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void addServiceCard(Service service) {

        LinearLayout card =
                new LinearLayout(this);

        card.setOrientation(
                LinearLayout.VERTICAL
        );

        card.setPadding(
                20,
                20,
                20,
                20
        );

        TextView title =
                new TextView(this);

        title.setText(
                service.getTitle()
        );

        title.setTextColor(
                0xFF111827
        );

        title.setTextSize(20);

        title.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        TextView description =
                new TextView(this);

        description.setText(
                service.getDescription()
        );

        description.setTextColor(
                0xFF6B7280
        );

        description.setTextSize(14);

        description.setPadding(
                0,
                8,
                0,
                8
        );

        TextView category =
                new TextView(this);

        category.setText(
                "Category: " + service.getCategory()
        );

        category.setTextColor(
                0xFF374151
        );

        category.setTextSize(14);

        TextView price =
                new TextView(this);

        price.setText(
                String.format(
                        "Price: ₹%.2f",
                        service.getPrice()
                )
        );

        price.setTextColor(
                0xFF111827
        );

        price.setTextSize(16);

        price.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        TextView status =
                new TextView(this);

        status.setText(
                "Status: " + service.getStatus()
        );

        status.setTextColor(
                0xFF374151
        );

        status.setTextSize(14);

        Button editButton =
                new Button(this);

        editButton.setText(
                "Edit Service"
        );

        editButton.setAllCaps(false);

        editButton.setOnClickListener(
                v -> {

                    android.content.Intent intent =
                            new android.content.Intent(
                                    MyServicesActivity.this,
                                    EditServiceActivity.class
                            );

                    intent.putExtra(
                            "serviceId",
                            service.getId()
                    );

                    startActivity(intent);
                }
        );

        Button deleteButton =
                new Button(this);

        deleteButton.setText(
                "Delete Service"
        );

        deleteButton.setAllCaps(false);

        deleteButton.setOnClickListener(
                v -> deleteService(service)
        );

        deleteButton.setAllCaps(false);

        deleteButton.setOnClickListener(
                v -> deleteService(service)
        );

        card.addView(title);
        card.addView(description);
        card.addView(category);
        card.addView(price);
        card.addView(status);
        card.addView(editButton);
        card.addView(deleteButton);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        params.setMargins(
                0,
                0,
                0,
                16
        );

        myServicesContainer.addView(
                card,
                params
        );
    }

    private void deleteService(Service service) {

        String token = sessionManager.getToken();

        if (token == null || token.isEmpty()) {
            return;
        }

        ServicesApi api =
                RetrofitClient.getInstance()
                        .create(ServicesApi.class);

        api.deleteService(
                service.getId(),
                "Bearer " + token
        ).enqueue(new Callback<okhttp3.ResponseBody>() {

            @Override
            public void onResponse(
                    Call<okhttp3.ResponseBody> call,
                    Response<okhttp3.ResponseBody> response) {

                if (response.isSuccessful()) {

                    Toast.makeText(
                            MyServicesActivity.this,
                            "Service deleted successfully",
                            Toast.LENGTH_SHORT
                    ).show();

                    loadMyServices();

                } else {

                    Toast.makeText(
                            MyServicesActivity.this,
                            "Failed to delete service",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }

            @Override
            public void onFailure(
                    Call<okhttp3.ResponseBody> call,
                    Throwable t) {

                Toast.makeText(
                        MyServicesActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }
}