package com.example.skillhive;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.skillhive.model.Notification;
import com.example.skillhive.network.NotificationApi;
import com.example.skillhive.network.RetrofitClient;
import com.example.skillhive.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.Calendar;

public class HomeActivity extends AppCompatActivity {

    private TextView tvWelcome;

    private Button btnFindServices;
    private Button btnOfferService;
    private Button btnMyServices;
    private Button btnReceivedOrders;
    private Button btnMyOrders;
    private Button btnWallet;
    private Button btnProfile;
    private Button btnFavorites;
    private Button btnNotifications;
    private Button btnLogout;

    private NotificationApi notificationApi;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);

        tvWelcome = findViewById(R.id.tvWelcome);

        btnFindServices = findViewById(R.id.btnFindServices);
        btnOfferService = findViewById(R.id.btnOfferService);
        btnMyServices = findViewById(R.id.btnMyServices);
        btnReceivedOrders = findViewById(R.id.btnReceivedOrders);
        btnMyOrders = findViewById(R.id.btnMyOrders);
        btnWallet = findViewById(R.id.btnWallet);
        btnProfile = findViewById(R.id.btnProfile);
        btnFavorites = findViewById(R.id.btnFavorites);
        btnNotifications = findViewById(R.id.btnNotifications);
        btnLogout = findViewById(R.id.btnLogout);

        notificationApi =
                RetrofitClient.getInstance()
                        .create(NotificationApi.class);

        sessionManager = new SessionManager(this);

        // Set personalized greeting
        setPersonalizedGreeting();

        loadUnreadNotificationCount();

        // Logout
        btnLogout.setOnClickListener(v -> {

            sessionManager.logout();

            Intent intent =
                    new Intent(
                            HomeActivity.this,
                            MainActivity.class
                    );

            intent.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK
            );

            startActivity(intent);
        });

        // Find Services
        btnFindServices.setOnClickListener(v -> {
            startActivity(
                    new Intent(
                            HomeActivity.this,
                            ServicesActivity.class
                    )
            );
        });

        // Offer Service
        btnOfferService.setOnClickListener(v -> {
            startActivity(
                    new Intent(
                            HomeActivity.this,
                            OfferServiceActivity.class
                    )
            );
        });

        // My Services
        btnMyServices.setOnClickListener(v -> {
            startActivity(
                    new Intent(
                            HomeActivity.this,
                            MyServicesActivity.class
                    )
            );
        });

        // Received Orders
        btnReceivedOrders.setOnClickListener(v -> {
            startActivity(
                    new Intent(
                            HomeActivity.this,
                            MyReceivedOrdersActivity.class
                    )
            );
        });

        // My Orders
        btnMyOrders.setOnClickListener(v -> {
            startActivity(
                    new Intent(
                            HomeActivity.this,
                            MyOrdersActivity.class
                    )
            );
        });

        // Wallet
        btnWallet.setOnClickListener(v -> {
            startActivity(
                    new Intent(
                            HomeActivity.this,
                            WalletActivity.class
                    )
            );
        });

        // Profile
        btnProfile.setOnClickListener(v -> {
            startActivity(
                    new Intent(
                            HomeActivity.this,
                            ProfileActivity.class
                    )
            );
        });

        // Favorites
        btnFavorites.setOnClickListener(v -> {
            startActivity(
                    new Intent(
                            HomeActivity.this,
                            FavoritesActivity.class
                    )
            );
        });

        // Notifications
        btnNotifications.setOnClickListener(v -> {

            startActivity(
                    new Intent(
                            HomeActivity.this,
                            NotificationsActivity.class
                    )
            );
        });
    }

    /**
     * Sets the greeting according to the current time.
     */
    private void setPersonalizedGreeting() {

        String fullName = sessionManager.getFullName();

        if (fullName == null || fullName.trim().isEmpty()) {
            fullName = "there";
        }

        Calendar calendar = Calendar.getInstance();

        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        String greeting;

        if (hour >= 5 && hour < 12) {
            greeting = "Good Morning";
        } else if (hour >= 12 && hour < 17) {
            greeting = "Good Afternoon";
        } else {
            greeting = "Good Evening";
        }

        tvWelcome.setText(
                greeting + ",\n" + fullName
        );
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Refresh greeting when returning to Home
        if (sessionManager != null) {
            setPersonalizedGreeting();
        }

        if (notificationApi != null) {
            loadUnreadNotificationCount();
        }
    }

    private void loadUnreadNotificationCount() {

        String token = sessionManager.getToken();

        if (token == null || token.isEmpty()) {
            return;
        }

        notificationApi
                .getUnreadCount("Bearer " + token)
                .enqueue(new Callback<Long>() {

                    @Override
                    public void onResponse(
                            Call<Long> call,
                            Response<Long> response) {

                        if (!response.isSuccessful()
                                || response.body() == null) {
                            return;
                        }

                        long count = response.body();

                        if (count > 0) {

                            btnNotifications.setText(
                                    "🔔 Notifications (" + count + ")"
                            );

                        } else {

                            btnNotifications.setText(
                                    "🔔 Notifications"
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<Long> call,
                            Throwable t) {

                        // Keep normal notification button text
                    }
                });
    }
}