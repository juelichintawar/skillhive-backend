package com.example.skillhive;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.skillhive.model.Favorite;
import com.example.skillhive.model.Review;
import com.example.skillhive.model.Service;
import com.example.skillhive.network.FavoritesApi;
import com.example.skillhive.network.OrdersApi;
import com.example.skillhive.network.RetrofitClient;
import com.example.skillhive.network.ReviewApi;
import com.example.skillhive.network.ServicesApi;
import com.example.skillhive.utils.SessionManager;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ServiceDetailsActivity extends AppCompatActivity {

    private TextView tvDetailTitle;
    private TextView tvDetailDescription;
    private TextView tvDetailCategory;
    private TextView tvDetailPrice;
    private TextView tvDetailProvider;
    private TextView tvDetailCollege;
    private TextView tvDetailRating;

    private LinearLayout reviewsContainer;

    private Button btnRequestService;
    private Button btnViewProvider;
    private Button btnFavorite;

    private ServicesApi servicesApi;
    private OrdersApi ordersApi;
    private ReviewApi reviewApi;
    private FavoritesApi favoritesApi;

    private SessionManager sessionManager;

    private Long serviceId;

    private boolean isFavorite = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_service_details);

        // =========================
        // FIND VIEWS
        // =========================

        tvDetailTitle = findViewById(R.id.tvDetailTitle);
        tvDetailDescription = findViewById(R.id.tvDetailDescription);
        tvDetailCategory = findViewById(R.id.tvDetailCategory);
        tvDetailPrice = findViewById(R.id.tvDetailPrice);
        tvDetailProvider = findViewById(R.id.tvDetailProvider);
        tvDetailCollege = findViewById(R.id.tvDetailCollege);
        tvDetailRating = findViewById(R.id.tvDetailRating);

        reviewsContainer = findViewById(R.id.reviewsContainer);

        btnRequestService = findViewById(R.id.btnRequestService);
        btnViewProvider = findViewById(R.id.btnViewProvider);
        btnFavorite = findViewById(R.id.btnFavorite);

        // =========================
        // GET SERVICE ID
        // =========================

        serviceId = getIntent().getLongExtra("serviceId", -1);

        if (serviceId == -1) {

            Toast.makeText(
                    this,
                    "Service not found",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        // =========================
        // API INITIALIZATION
        // =========================

        servicesApi = RetrofitClient
                .getInstance()
                .create(ServicesApi.class);

        ordersApi = RetrofitClient
                .getInstance()
                .create(OrdersApi.class);

        reviewApi = RetrofitClient
                .getInstance()
                .create(ReviewApi.class);

        favoritesApi = RetrofitClient
                .getInstance()
                .create(FavoritesApi.class);

        sessionManager = new SessionManager(this);

        // =========================
        // LOAD DATA
        // =========================

        loadServiceDetails();
        loadFavoriteStatus();

        // =========================
        // BUTTON LISTENERS
        // =========================

        btnRequestService.setOnClickListener(
                v -> requestService()
        );

        btnViewProvider.setOnClickListener(
                v -> openProviderProfile()
        );

        btnFavorite.setOnClickListener(
                v -> toggleFavorite()
        );
    }

    // =========================================================
    // SERVICE DETAILS
    // =========================================================

    private void loadServiceDetails() {

        servicesApi
                .getServiceById(serviceId)
                .enqueue(new Callback<Service>() {

                    @Override
                    public void onResponse(
                            Call<Service> call,
                            Response<Service> response) {

                        if (response.isSuccessful()
                                && response.body() != null) {

                            Service service = response.body();

                            displayServiceDetails(service);

                        } else {

                            Toast.makeText(
                                    ServiceDetailsActivity.this,
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

                        Toast.makeText(
                                ServiceDetailsActivity.this,
                                "Connection failed: "
                                        + t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();

                        finish();
                    }
                });
    }

    private void displayServiceDetails(Service service) {

        tvDetailTitle.setText(
                getSafeText(
                        service.getTitle(),
                        "Untitled Service"
                )
        );

        tvDetailDescription.setText(
                getSafeText(
                        service.getDescription(),
                        "No description available."
                )
        );

        tvDetailCategory.setText(
                "Category: "
                        + getSafeText(
                        service.getCategory(),
                        "General"
                )
        );

        double price =
                service.getPrice() != null
                        ? service.getPrice()
                        : 0.0;

        tvDetailPrice.setText(
                String.format(
                        Locale.US,
                        "Price: ₹%.2f",
                        price
                )
        );

        if (service.getProvider() != null) {

            String providerName =
                    service.getProvider().getFullName();

            String college =
                    service.getProvider().getCollege();

            tvDetailProvider.setText(
                    "Provider: "
                            + getSafeText(
                            providerName,
                            "Unknown"
                    )
            );

            tvDetailCollege.setText(
                    "College: "
                            + getSafeText(
                            college,
                            "Unknown"
                    )
            );

            Long providerId =
                    service.getProvider().getId();

            if (providerId != null) {

                loadProviderRating(providerId);
                loadProviderReviews(providerId);

            } else {

                tvDetailRating.setText(
                        "⭐ 0.0 (0 reviews)"
                );

                showNoReviews();
            }

        } else {

            tvDetailProvider.setText(
                    "Provider: Unknown"
            );

            tvDetailCollege.setText(
                    "College: Unknown"
            );

            tvDetailRating.setText(
                    "⭐ 0.0 (0 reviews)"
            );

            showNoReviews();
        }
    }

    // =========================================================
    // FAVORITES
    // =========================================================

    private void loadFavoriteStatus() {

        String token = sessionManager.getToken();

        if (token == null || token.isEmpty()) {

            isFavorite = false;
            updateFavoriteButton();

            return;
        }

        favoritesApi
                .getMyFavorites("Bearer " + token)
                .enqueue(new Callback<List<Favorite>>() {

                    @Override
                    public void onResponse(
                            Call<List<Favorite>> call,
                            Response<List<Favorite>> response) {

                        if (!response.isSuccessful()
                                || response.body() == null) {

                            isFavorite = false;
                            updateFavoriteButton();

                            return;
                        }

                        isFavorite = false;

                        for (Favorite favorite : response.body()) {

                            if (favorite == null
                                    || favorite.getService() == null
                                    || favorite.getService().getId() == null) {

                                continue;
                            }

                            if (serviceId.equals(
                                    favorite.getService().getId())) {

                                isFavorite = true;
                                break;
                            }
                        }

                        updateFavoriteButton();
                    }

                    @Override
                    public void onFailure(
                            Call<List<Favorite>> call,
                            Throwable t) {

                        isFavorite = false;
                        updateFavoriteButton();
                    }
                });
    }

    private void updateFavoriteButton() {

        if (btnFavorite == null) {
            return;
        }

        if (isFavorite) {

            btnFavorite.setText(
                    "♥ Remove from Favorites"
            );

        } else {

            btnFavorite.setText(
                    "♡ Add to Favorites"
            );
        }
    }

    private void toggleFavorite() {

        String token = sessionManager.getToken();

        if (token == null || token.isEmpty()) {

            Toast.makeText(
                    this,
                    "Please login to use favorites",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        btnFavorite.setEnabled(false);

        if (isFavorite) {
            removeFavorite(token);
        } else {
            addFavorite(token);
        }
    }

    private void addFavorite(String token) {

        favoritesApi
                .addFavorite(
                        serviceId,
                        "Bearer " + token
                )
                .enqueue(new Callback<Favorite>() {

                    @Override
                    public void onResponse(
                            Call<Favorite> call,
                            Response<Favorite> response) {

                        btnFavorite.setEnabled(true);

                        if (response.isSuccessful()) {

                            isFavorite = true;

                            updateFavoriteButton();

                            Toast.makeText(
                                    ServiceDetailsActivity.this,
                                    "Added to favorites",
                                    Toast.LENGTH_SHORT
                            ).show();

                        } else {

                            updateFavoriteButton();

                            String message =
                                    "Failed to add favorite";

                            try {

                                if (response.errorBody() != null) {

                                    message =
                                            response.errorBody().string();
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            Toast.makeText(
                                    ServiceDetailsActivity.this,
                                    message,
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<Favorite> call,
                            Throwable t) {

                        btnFavorite.setEnabled(true);

                        Toast.makeText(
                                ServiceDetailsActivity.this,
                                "Connection failed",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void removeFavorite(String token) {

        favoritesApi
                .removeFavorite(
                        serviceId,
                        "Bearer " + token
                )
                .enqueue(new Callback<Void>() {

                    @Override
                    public void onResponse(
                            Call<Void> call,
                            Response<Void> response) {

                        btnFavorite.setEnabled(true);

                        if (response.isSuccessful()) {

                            isFavorite = false;

                            updateFavoriteButton();

                            Toast.makeText(
                                    ServiceDetailsActivity.this,
                                    "Removed from favorites",
                                    Toast.LENGTH_SHORT
                            ).show();

                        } else {

                            updateFavoriteButton();

                            Toast.makeText(
                                    ServiceDetailsActivity.this,
                                    "Failed to remove favorite",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<Void> call,
                            Throwable t) {

                        btnFavorite.setEnabled(true);

                        updateFavoriteButton();

                        Toast.makeText(
                                ServiceDetailsActivity.this,
                                "Connection failed",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    // =========================================================
    // RATING
    // =========================================================

    private void loadProviderRating(Long providerId) {

        tvDetailRating.setText(
                "⭐ Loading rating..."
        );

        reviewApi
                .getProviderReviews(providerId)
                .enqueue(new Callback<List<Review>>() {

                    @Override
                    public void onResponse(
                            Call<List<Review>> call,
                            Response<List<Review>> response) {

                        if (response.isSuccessful()
                                && response.body() != null) {

                            List<Review> reviews =
                                    response.body();

                            if (reviews.isEmpty()) {

                                tvDetailRating.setText(
                                        "⭐ 0.0 (0 reviews)"
                                );

                                return;
                            }

                            double totalRating = 0.0;
                            int validReviews = 0;

                            for (Review review : reviews) {

                                if (review != null
                                        && review.getRating() != null) {

                                    totalRating +=
                                            review.getRating();

                                    validReviews++;
                                }
                            }

                            if (validReviews == 0) {

                                tvDetailRating.setText(
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

                            tvDetailRating.setText(
                                    String.format(
                                            Locale.US,
                                            "⭐ %.1f (%d %s)",
                                            averageRating,
                                            validReviews,
                                            reviewText
                                    )
                            );

                        } else {

                            tvDetailRating.setText(
                                    "⭐ Rating unavailable"
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<List<Review>> call,
                            Throwable t) {

                        tvDetailRating.setText(
                                "⭐ Rating unavailable"
                        );
                    }
                });
    }

    // =========================================================
    // REVIEWS
    // =========================================================

    private void loadProviderReviews(Long providerId) {

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

                        reviewsContainer.removeAllViews();

                        if (reviews.isEmpty()) {

                            showNoReviews();
                            return;
                        }

                        for (Review review : reviews) {

                            if (review == null) {
                                continue;
                            }

                            TextView reviewText =
                                    new TextView(
                                            ServiceDetailsActivity.this
                                    );

                            StringBuilder stars =
                                    new StringBuilder();

                            if (review.getRating() != null) {

                                for (int i = 0;
                                     i < review.getRating();
                                     i++) {

                                    stars.append("★");
                                }
                            }

                            String comment =
                                    review.getComment();

                            if (comment == null
                                    || comment.trim().isEmpty()) {

                                comment =
                                        "No comment provided.";
                            }

                            reviewText.setText(
                                    stars
                                            + "\n"
                                            + comment
                            );

                            reviewText.setTextSize(15);

                            reviewText.setTextColor(
                                    0xFF111827
                            );

                            reviewText.setPadding(
                                    0,
                                    12,
                                    0,
                                    12
                            );

                            reviewsContainer.addView(
                                    reviewText
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<List<Review>> call,
                            Throwable t) {

                        showNoReviews();
                    }
                });
    }

    private void showNoReviews() {

        if (reviewsContainer == null) {
            return;
        }

        reviewsContainer.removeAllViews();

        TextView emptyText =
                new TextView(this);

        emptyText.setText(
                "No reviews yet."
        );

        emptyText.setTextSize(15);

        emptyText.setTextColor(
                0xFF6B7280
        );

        emptyText.setPadding(
                0,
                8,
                0,
                8
        );

        reviewsContainer.addView(
                emptyText
        );
    }

    // =========================================================
    // PROVIDER PROFILE
    // =========================================================

    private void openProviderProfile() {

        if (serviceId == -1) {

            Toast.makeText(
                    this,
                    "Service not found",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        servicesApi
                .getServiceById(serviceId)
                .enqueue(new Callback<Service>() {

                    @Override
                    public void onResponse(
                            Call<Service> call,
                            Response<Service> response) {

                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().getProvider() != null) {

                            Long providerId =
                                    response.body()
                                            .getProvider()
                                            .getId();

                            if (providerId == null) {

                                Toast.makeText(
                                        ServiceDetailsActivity.this,
                                        "Provider not found",
                                        Toast.LENGTH_SHORT
                                ).show();

                                return;
                            }

                            Intent intent =
                                    new Intent(
                                            ServiceDetailsActivity.this,
                                            ProviderProfileActivity.class
                                    );

                            intent.putExtra(
                                    "providerId",
                                    providerId
                            );

                            startActivity(intent);

                        } else {

                            Toast.makeText(
                                    ServiceDetailsActivity.this,
                                    "Provider not found",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<Service> call,
                            Throwable t) {

                        Toast.makeText(
                                ServiceDetailsActivity.this,
                                "Failed to load provider",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    // =========================================================
    // REQUEST SERVICE
    // =========================================================

    private void requestService() {

        if (serviceId == -1) {

            Toast.makeText(
                    this,
                    "Service not found",
                    Toast.LENGTH_SHORT
            ).show();

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

        showProjectRequirementsDialog(token);
    }

    // =========================================================
    // POLISHED PROJECT REQUEST FORM
    // =========================================================

    private void showProjectRequirementsDialog(String token) {

        // Inflate our custom XML layout
        View dialogView = getLayoutInflater().inflate(
                R.layout.dialog_project_request,
                null
        );

        // =========================
        // FIND FORM VIEWS
        // =========================

        EditText etProjectTitle =
                dialogView.findViewById(
                        R.id.etProjectTitle
                );

        EditText etRequirements =
                dialogView.findViewById(
                        R.id.etRequirements
                );

        EditText etDeadline =
                dialogView.findViewById(
                        R.id.etDeadline
                );

        Button btnCancelRequest =
                dialogView.findViewById(
                        R.id.btnCancelRequest
                );

        Button btnSubmitRequest =
                dialogView.findViewById(
                        R.id.btnSubmitRequest
                );

        // =========================
        // CREATE CUSTOM DIALOG
        // =========================

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setView(dialogView)
                        .create();

        // =========================
        // MAKE DIALOG TRANSPARENT
        // =========================

        if (dialog.getWindow() != null) {

            dialog.getWindow().setBackgroundDrawableResource(
                    android.R.color.transparent
            );
        }

        // =========================
        // DEADLINE DATE PICKER
        // =========================

        etDeadline.setOnClickListener(v -> {

            Calendar calendar =
                    Calendar.getInstance();

            DatePickerDialog datePickerDialog =
                    new DatePickerDialog(
                            ServiceDetailsActivity.this,

                            (view, year, month, dayOfMonth) -> {

                                String selectedDate =
                                        String.format(
                                                Locale.US,
                                                "%04d-%02d-%02d",
                                                year,
                                                month + 1,
                                                dayOfMonth
                                        );

                                etDeadline.setText(
                                        selectedDate
                                );
                            },

                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                    );

            // Prevent past dates
            datePickerDialog
                    .getDatePicker()
                    .setMinDate(
                            System.currentTimeMillis()
                    );

            datePickerDialog.show();
        });

        // =========================
        // CANCEL
        // =========================

        btnCancelRequest.setOnClickListener(v ->
                dialog.dismiss()
        );

        // =========================
        // SUBMIT
        // =========================

        btnSubmitRequest.setOnClickListener(v -> {

            String projectTitle =
                    etProjectTitle
                            .getText()
                            .toString()
                            .trim();

            String requirements =
                    etRequirements
                            .getText()
                            .toString()
                            .trim();

            String deadline =
                    etDeadline
                            .getText()
                            .toString()
                            .trim();

            // =========================
            // VALIDATION
            // =========================

            if (projectTitle.isEmpty()) {

                etProjectTitle.setError(
                        "Project title is required"
                );

                etProjectTitle.requestFocus();

                return;
            }

            if (requirements.isEmpty()) {

                etRequirements.setError(
                        "Project requirements are required"
                );

                etRequirements.requestFocus();

                return;
            }

            if (deadline.isEmpty()) {

                etDeadline.setError(
                        "Please select a deadline"
                );

                etDeadline.requestFocus();

                return;
            }

            // =========================
            // CLOSE DIALOG
            // =========================

            dialog.dismiss();

            // =========================
            // CREATE ORDER
            // =========================

            createOrder(
                    projectTitle,
                    requirements,
                    deadline,
                    token
            );
        });

        // =========================
        // SHOW DIALOG
        // =========================

        dialog.show();

        // =========================
        // DIALOG SIZE
        // =========================

        if (dialog.getWindow() != null) {

            dialog.getWindow().setLayout(
                    (int) (getResources()
                            .getDisplayMetrics()
                            .widthPixels * 0.92),

                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
        }
    }

    // =========================================================
    // CREATE ORDER
    // =========================================================

    private void createOrder(
            String projectTitle,
            String requirements,
            String deadline,
            String token) {

        btnRequestService.setEnabled(false);

        btnRequestService.setText(
                "Requesting..."
        );

        ordersApi
                .createOrder(
                        serviceId,
                        projectTitle,
                        requirements,
                        deadline,
                        "Bearer " + token
                )
                .enqueue(new Callback<ResponseBody>() {

                    @Override
                    public void onResponse(
                            Call<ResponseBody> call,
                            Response<ResponseBody> response) {

                        btnRequestService.setEnabled(true);

                        btnRequestService.setText(
                                "Request Service"
                        );

                        if (response.isSuccessful()) {

                            Toast.makeText(
                                    ServiceDetailsActivity.this,
                                    "Service requested successfully!",
                                    Toast.LENGTH_LONG
                            ).show();

                        } else {

                            String errorMessage =
                                    "Request failed: "
                                            + response.code();

                            try {

                                if (response.errorBody() != null) {

                                    errorMessage =
                                            response.errorBody()
                                                    .string();
                                }

                            } catch (Exception e) {

                                e.printStackTrace();
                            }

                            Toast.makeText(
                                    ServiceDetailsActivity.this,
                                    errorMessage,
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<ResponseBody> call,
                            Throwable t) {

                        btnRequestService.setEnabled(true);

                        btnRequestService.setText(
                                "Request Service"
                        );

                        Toast.makeText(
                                ServiceDetailsActivity.this,
                                "Connection failed: "
                                        + t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    // =========================================================
    // UTILITY
    // =========================================================

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