package com.example.skillhive;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.skillhive.model.Review;
import com.example.skillhive.model.Service;
import com.example.skillhive.network.RetrofitClient;
import com.example.skillhive.network.ReviewApi;
import com.example.skillhive.network.ServicesApi;

import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProviderProfileActivity extends AppCompatActivity {

    private TextView tvProviderName;
    private TextView tvProviderCollege;
    private TextView tvProviderBio;
    private TextView tvProviderRating;

    private LinearLayout providerServicesContainer;
    private LinearLayout providerReviewsContainer;

    private ServicesApi servicesApi;
    private ReviewApi reviewApi;

    private Long providerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_provider_profile);

        tvProviderName = findViewById(R.id.tvProviderName);
        tvProviderCollege = findViewById(R.id.tvProviderCollege);
        tvProviderBio = findViewById(R.id.tvProviderBio);
        tvProviderRating = findViewById(R.id.tvProviderRating);

        providerServicesContainer =
                findViewById(R.id.providerServicesContainer);

        providerReviewsContainer =
                findViewById(R.id.providerReviewsContainer);

        providerId = getIntent().getLongExtra("providerId", -1);

        if (providerId == -1) {
            finish();
            return;
        }

        servicesApi = RetrofitClient
                .getInstance()
                .create(ServicesApi.class);

        reviewApi = RetrofitClient
                .getInstance()
                .create(ReviewApi.class);

        loadProviderServices();
        loadProviderReviews();
    }

    private void loadProviderServices() {

        servicesApi.getAllServices()
                .enqueue(new Callback<List<Service>>() {

                    @Override
                    public void onResponse(
                            Call<List<Service>> call,
                            Response<List<Service>> response) {

                        if (!response.isSuccessful()
                                || response.body() == null) {
                            return;
                        }

                        List<Service> services = response.body();

                        providerServicesContainer.removeAllViews();

                        boolean foundProvider = false;

                        for (Service service : services) {

                            if (service == null
                                    || service.getProvider() == null
                                    || service.getProvider().getId() == null) {
                                continue;
                            }

                            if (!providerId.equals(
                                    service.getProvider().getId())) {
                                continue;
                            }

                            foundProvider = true;

                            displayProviderInfo(service);
                            addServiceCard(service);
                        }

                        if (!foundProvider) {

                            tvProviderName.setText("Provider");
                            tvProviderCollege.setText(
                                    "College: Unknown"
                            );
                            tvProviderBio.setText(
                                    "Bio: No bio available."
                            );

                            TextView emptyText =
                                    new TextView(
                                            ProviderProfileActivity.this
                                    );

                            emptyText.setText(
                                    "No services available."
                            );

                            emptyText.setTextSize(15);
                            emptyText.setTextColor(0xFF64748B);
                            emptyText.setGravity(
                                    android.view.Gravity.CENTER
                            );
                            emptyText.setPadding(
                                    20,
                                    24,
                                    20,
                                    24
                            );
                            emptyText.setBackgroundResource(
                                    R.drawable.bg_empty_state
                            );

                            providerServicesContainer
                                    .addView(emptyText);
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<List<Service>> call,
                            Throwable t) {
                        // Keep screen usable
                    }
                });
    }

    private void displayProviderInfo(Service service) {

        if (service.getProvider() == null) {
            return;
        }

        String providerName =
                service.getProvider().getFullName();

        String college =
                service.getProvider().getCollege();

        String bio =
                service.getProvider().getBio();

        tvProviderName.setText(
                getSafeText(
                        providerName,
                        "Unknown Provider"
                )
        );

        tvProviderCollege.setText(
                "College: "
                        + getSafeText(
                        college,
                        "Unknown"
                )
        );

        tvProviderBio.setText(
                "Bio: "
                        + getSafeText(
                        bio,
                        "No bio available."
                )
        );
    }

    private void addServiceCard(Service service) {

        LinearLayout serviceCard =
                new LinearLayout(this);

        serviceCard.setOrientation(
                LinearLayout.VERTICAL
        );

        serviceCard.setPadding(
                20,
                18,
                20,
                18
        );

        serviceCard.setBackgroundResource(
                R.drawable.bg_service_card
        );

        // Service title

        TextView titleView =
                new TextView(this);

        titleView.setText(
                getSafeText(
                        service.getTitle(),
                        "Untitled Service"
                )
        );

        titleView.setTextSize(17);
        titleView.setTextColor(0xFF1264E5);
        titleView.setTypeface(
                null,
                Typeface.BOLD
        );

        serviceCard.addView(titleView);

        // Service information row

        LinearLayout infoRow =
                new LinearLayout(this);

        infoRow.setOrientation(
                LinearLayout.HORIZONTAL
        );

        infoRow.setGravity(
                android.view.Gravity.CENTER_VERTICAL
        );

        LinearLayout.LayoutParams infoRowParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        infoRowParams.setMargins(
                0,
                10,
                0,
                0
        );

        infoRow.setLayoutParams(infoRowParams);

        // Category

        TextView categoryView =
                new TextView(this);

        String category =
                getSafeText(
                        service.getCategory(),
                        "General"
                );

        categoryView.setText(
                "Category  •  " + category
        );

        categoryView.setTextSize(13);
        categoryView.setTextColor(0xFF64748B);

        LinearLayout.LayoutParams categoryParams =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                );

        categoryView.setLayoutParams(
                categoryParams
        );

        infoRow.addView(categoryView);

        // Price

        TextView priceView =
                new TextView(this);

        double price =
                service.getPrice() != null
                        ? service.getPrice()
                        : 0.0;

        priceView.setText(
                String.format(
                        Locale.US,
                        "₹%.2f",
                        price
                )
        );

        priceView.setTextSize(16);
        priceView.setTextColor(0xFF0F172A);
        priceView.setTypeface(
                null,
                Typeface.BOLD
        );

        infoRow.addView(priceView);

        serviceCard.addView(infoRow);

        // Add spacing between cards

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        cardParams.setMargins(
                0,
                0,
                0,
                12
        );

        serviceCard.setLayoutParams(cardParams);

        providerServicesContainer.addView(
                serviceCard
        );
    }

    private void loadProviderReviews() {

        reviewApi
                .getProviderReviews(providerId)
                .enqueue(new Callback<List<Review>>() {

                    @Override
                    public void onResponse(
                            Call<List<Review>> call,
                            Response<List<Review>> response) {

                        if (!response.isSuccessful()
                                || response.body() == null) {

                            showNoReviews();
                            return;
                        }

                        List<Review> reviews =
                                response.body();

                        calculateRating(reviews);
                        displayReviews(reviews);
                    }

                    @Override
                    public void onFailure(
                            Call<List<Review>> call,
                            Throwable t) {

                        tvProviderRating.setText(
                                "⭐ Rating unavailable"
                        );

                        showNoReviews();
                    }
                });
    }

    private void calculateRating(List<Review> reviews) {

        if (reviews.isEmpty()) {

            tvProviderRating.setText(
                    "⭐ 0.0 (0 reviews)"
            );

            return;
        }

        double totalRating = 0.0;
        int validReviews = 0;

        for (Review review : reviews) {

            if (review != null
                    && review.getRating() != null) {

                totalRating += review.getRating();
                validReviews++;
            }
        }

        if (validReviews == 0) {

            tvProviderRating.setText(
                    "⭐ 0.0 (0 reviews)"
            );

            return;
        }

        double averageRating =
                totalRating / validReviews;

        String reviewText =
                validReviews == 1
                        ? "review"
                        : "reviews";

        tvProviderRating.setText(
                String.format(
                        Locale.US,
                        "⭐ %.1f (%d %s)",
                        averageRating,
                        validReviews,
                        reviewText
                )
        );
    }

    private void displayReviews(List<Review> reviews) {

        providerReviewsContainer.removeAllViews();

        if (reviews.isEmpty()) {
            showNoReviews();
            return;
        }

        for (Review review : reviews) {

            if (review == null) {
                continue;
            }

            addReviewCard(review);
        }
    }

    private void addReviewCard(Review review) {

        LinearLayout reviewCard =
                new LinearLayout(this);

        reviewCard.setOrientation(
                LinearLayout.VERTICAL
        );

        reviewCard.setPadding(
                20,
                18,
                20,
                18
        );

        reviewCard.setBackgroundResource(
                R.drawable.bg_detail_card
        );

        // Rating stars

        TextView starsView =
                new TextView(this);

        StringBuilder stars =
                new StringBuilder();

        if (review.getRating() != null) {

            for (int i = 0;
                 i < review.getRating();
                 i++) {

                stars.append("★");
            }
        }

        starsView.setText(
                stars.length() > 0
                        ? stars.toString()
                        : "No rating"
        );

        starsView.setTextSize(17);
        starsView.setTextColor(0xFFF59E0B);
        starsView.setTypeface(
                null,
                Typeface.BOLD
        );

        reviewCard.addView(starsView);

        // Review comment

        String comment =
                review.getComment();

        if (comment == null
                || comment.trim().isEmpty()) {

            comment = "No comment provided.";
        }

        TextView commentView =
                new TextView(this);

        commentView.setText(
                "\"" + comment + "\""
        );

        commentView.setTextSize(15);
        commentView.setTextColor(0xFF334155);
        commentView.setLineSpacing(
                0,
                1.1f
        );

        LinearLayout.LayoutParams commentParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        commentParams.setMargins(
                0,
                10,
                0,
                0
        );

        commentView.setLayoutParams(
                commentParams
        );

        reviewCard.addView(commentView);

        // Card spacing

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        cardParams.setMargins(
                0,
                0,
                0,
                12
        );

        reviewCard.setLayoutParams(cardParams);

        providerReviewsContainer.addView(
                reviewCard
        );
    }

    private void showNoReviews() {

        providerReviewsContainer.removeAllViews();

        TextView emptyText =
                new TextView(this);

        emptyText.setText(
                "No reviews yet."
        );

        emptyText.setTextSize(15);
        emptyText.setTextColor(0xFF64748B);
        emptyText.setGravity(
                android.view.Gravity.CENTER
        );

        emptyText.setPadding(
                20,
                24,
                20,
                24
        );

        emptyText.setBackgroundResource(
                R.drawable.bg_empty_state
        );

        providerReviewsContainer
                .addView(emptyText);
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