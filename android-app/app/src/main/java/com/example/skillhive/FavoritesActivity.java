package com.example.skillhive;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.skillhive.model.Favorite;
import com.example.skillhive.model.Service;
import com.example.skillhive.network.FavoritesApi;
import com.example.skillhive.network.RetrofitClient;
import com.example.skillhive.utils.SessionManager;

import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavoritesActivity extends AppCompatActivity {

    private LinearLayout favoritesContainer;
    private TextView tvNoFavorites;

    private FavoritesApi favoritesApi;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_favorites);

        favoritesContainer =
                findViewById(R.id.favoritesContainer);

        tvNoFavorites =
                findViewById(R.id.tvNoFavorites);

        favoritesApi =
                RetrofitClient
                        .getInstance()
                        .create(FavoritesApi.class);

        sessionManager =
                new SessionManager(this);

        loadFavorites();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (favoritesApi != null) {
            loadFavorites();
        }
    }

    private void loadFavorites() {

        String token =
                sessionManager.getToken();

        if (token == null
                || token.isEmpty()) {

            Toast.makeText(
                    this,
                    "Please login again",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        favoritesApi
                .getMyFavorites(
                        "Bearer " + token
                )
                .enqueue(
                        new Callback<List<Favorite>>() {

                            @Override
                            public void onResponse(
                                    Call<List<Favorite>> call,
                                    Response<List<Favorite>> response) {

                                if (!response.isSuccessful()
                                        || response.body() == null) {

                                    Toast.makeText(
                                            FavoritesActivity.this,
                                            "Failed to load favorites",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    return;
                                }

                                displayFavorites(
                                        response.body()
                                );
                            }

                            @Override
                            public void onFailure(
                                    Call<List<Favorite>> call,
                                    Throwable t) {

                                Toast.makeText(
                                        FavoritesActivity.this,
                                        "Connection failed",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }
                );
    }

    private void displayFavorites(
            List<Favorite> favorites) {

        favoritesContainer.removeAllViews();

        if (favorites == null
                || favorites.isEmpty()) {

            tvNoFavorites.setVisibility(
                    View.VISIBLE
            );

            return;
        }

        tvNoFavorites.setVisibility(
                View.GONE
        );

        for (Favorite favorite : favorites) {

            if (favorite == null
                    || favorite.getService() == null) {

                continue;
            }

            addFavoriteView(
                    favorite.getService()
            );
        }
    }

    private void addFavoriteView(
            Service service) {

        LinearLayout serviceCard =
                new LinearLayout(this);

        serviceCard.setOrientation(
                LinearLayout.VERTICAL
        );

        serviceCard.setPadding(
                16,
                16,
                16,
                16
        );

        TextView title =
                new TextView(this);

        title.setText(
                getSafeText(
                        service.getTitle(),
                        "Untitled Service"
                )
        );

        title.setTextSize(18);

        title.setTextColor(
                0xFF111827
        );

        title.setTypeface(
                null,
                Typeface.BOLD
        );

        TextView category =
                new TextView(this);

        category.setText(
                "Category: "
                        + getSafeText(
                        service.getCategory(),
                        "General"
                )
        );

        category.setTextSize(14);

        category.setTextColor(
                0xFF6B7280
        );

        category.setPadding(
                0,
                6,
                0,
                0
        );

        TextView price =
                new TextView(this);

        double servicePrice =
                service.getPrice() != null
                        ? service.getPrice()
                        : 0.0;

        price.setText(
                String.format(
                        Locale.US,
                        "₹%.2f",
                        servicePrice
                )
        );

        price.setTextSize(16);

        price.setTextColor(
                0xFF111827
        );

        price.setPadding(
                0,
                6,
                0,
                0
        );

        Button removeButton =
                new Button(this);

        removeButton.setText(
                "♥ Remove from Favorites"
        );

        removeButton.setAllCaps(false);

        removeButton.setOnClickListener(
                v -> removeFavorite(service)
        );

        serviceCard.addView(title);
        serviceCard.addView(category);
        serviceCard.addView(price);
        serviceCard.addView(removeButton);

        serviceCard.setOnClickListener(
                v -> openServiceDetails(service)
        );

        removeButton.setOnClickListener(
                v -> removeFavorite(service)
        );

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

        favoritesContainer.addView(
                serviceCard,
                params
        );
    }

    private void removeFavorite(
            Service service) {

        if (service == null
                || service.getId() == null) {

            Toast.makeText(
                    this,
                    "Service not found",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String token =
                sessionManager.getToken();

        if (token == null
                || token.isEmpty()) {

            Toast.makeText(
                    this,
                    "Please login again",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        favoritesApi
                .removeFavorite(
                        service.getId(),
                        "Bearer " + token
                )
                .enqueue(
                        new Callback<Void>() {

                            @Override
                            public void onResponse(
                                    Call<Void> call,
                                    Response<Void> response) {

                                if (response.isSuccessful()) {

                                    Toast.makeText(
                                            FavoritesActivity.this,
                                            "Removed from favorites",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    loadFavorites();

                                } else {

                                    Toast.makeText(
                                            FavoritesActivity.this,
                                            "Failed to remove favorite",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }
                            }

                            @Override
                            public void onFailure(
                                    Call<Void> call,
                                    Throwable t) {

                                Toast.makeText(
                                        FavoritesActivity.this,
                                        "Connection failed",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }
                );
    }

    private void openServiceDetails(
            Service service) {

        if (service.getId() == null) {

            Toast.makeText(
                    this,
                    "Service not found",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        Intent intent =
                new Intent(
                        FavoritesActivity.this,
                        ServiceDetailsActivity.class
                );

        intent.putExtra(
                "serviceId",
                service.getId()
        );

        startActivity(intent);
    }

    private String getSafeText(
            String value,
            String defaultValue) {

        if (value == null
                || value.trim().isEmpty()) {

            return defaultValue;
        }

        return value;
    }
}