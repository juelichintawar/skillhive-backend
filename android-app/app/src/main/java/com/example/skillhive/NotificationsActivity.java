package com.example.skillhive;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.skillhive.model.Notification;
import com.example.skillhive.network.NotificationApi;
import com.example.skillhive.network.RetrofitClient;
import com.example.skillhive.utils.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationsActivity extends AppCompatActivity {

    private LinearLayout notificationsContainer;
    private TextView tvNoNotifications;

    private NotificationApi notificationApi;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_notifications);

        notificationsContainer =
                findViewById(R.id.notificationsContainer);

        tvNoNotifications =
                findViewById(R.id.tvNoNotifications);

        notificationApi =
                RetrofitClient
                        .getInstance()
                        .create(NotificationApi.class);

        sessionManager =
                new SessionManager(this);

        loadNotifications();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (notificationApi != null) {
            loadNotifications();
        }
    }

    private void loadNotifications() {

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

        notificationApi
                .getMyNotifications(
                        "Bearer " + token
                )
                .enqueue(
                        new Callback<List<Notification>>() {

                            @Override
                            public void onResponse(
                                    Call<List<Notification>> call,
                                    Response<List<Notification>> response) {

                                if (!response.isSuccessful()
                                        || response.body() == null) {

                                    Toast.makeText(
                                            NotificationsActivity.this,
                                            "Failed to load notifications",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    return;
                                }

                                displayNotifications(
                                        response.body()
                                );
                            }

                            @Override
                            public void onFailure(
                                    Call<List<Notification>> call,
                                    Throwable t) {

                                Toast.makeText(
                                        NotificationsActivity.this,
                                        "Connection failed",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }
                );
    }

    private void displayNotifications(
            List<Notification> notifications) {

        notificationsContainer.removeAllViews();

        if (notifications == null
                || notifications.isEmpty()) {

            tvNoNotifications.setVisibility(
                    View.VISIBLE
            );

            return;
        }

        tvNoNotifications.setVisibility(
                View.GONE
        );

        for (Notification notification : notifications) {

            if (notification == null) {
                continue;
            }

            addNotificationView(notification);
        }
    }

    private void addNotificationView(
            Notification notification) {

        LinearLayout notificationCard =
                new LinearLayout(this);

        notificationCard.setOrientation(
                LinearLayout.VERTICAL
        );

        notificationCard.setPadding(
                16,
                16,
                16,
                16
        );

        TextView title =
                new TextView(this);

        title.setText(
                getSafeText(
                        notification.getTitle(),
                        "Notification"
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

        TextView message =
                new TextView(this);

        message.setText(
                getSafeText(
                        notification.getMessage(),
                        ""
                )
        );

        message.setTextSize(15);

        message.setTextColor(
                0xFF374151
        );

        message.setPadding(
                0,
                8,
                0,
                0
        );

        notificationCard.addView(title);
        notificationCard.addView(message);

        if (!notification.isRead()) {

            TextView unread =
                    new TextView(this);

            unread.setText(
                    "● Unread"
            );

            unread.setTextSize(13);

            unread.setTextColor(
                    0xFF2563EB
            );

            unread.setPadding(
                    0,
                    8,
                    0,
                    0
            );

            notificationCard.addView(unread);
        }

        if (notification.getId() != null
                && !notification.isRead()) {

            Button markReadButton =
                    new Button(this);

            markReadButton.setText(
                    "Mark as Read"
            );

            markReadButton.setAllCaps(false);

            markReadButton.setOnClickListener(
                    v -> markAsRead(notification)
            );

            notificationCard.addView(
                    markReadButton
            );
        }

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

        notificationsContainer.addView(
                notificationCard,
                params
        );
    }

    private void markAsRead(
            Notification notification) {

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

        notificationApi
                .markAsRead(
                        notification.getId(),
                        "Bearer " + token
                )
                .enqueue(
                        new Callback<Notification>() {

                            @Override
                            public void onResponse(
                                    Call<Notification> call,
                                    Response<Notification> response) {

                                if (response.isSuccessful()) {

                                    Toast.makeText(
                                            NotificationsActivity.this,
                                            "Notification marked as read",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    loadNotifications();

                                } else {

                                    Toast.makeText(
                                            NotificationsActivity.this,
                                            "Failed to mark notification as read",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }
                            }

                            @Override
                            public void onFailure(
                                    Call<Notification> call,
                                    Throwable t) {

                                Toast.makeText(
                                        NotificationsActivity.this,
                                        "Connection failed",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }
                );
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