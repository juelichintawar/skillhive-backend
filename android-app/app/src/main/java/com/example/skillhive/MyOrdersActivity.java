package com.example.skillhive;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.skillhive.model.Order;
import com.example.skillhive.network.OrdersApi;
import com.example.skillhive.network.RetrofitClient;
import com.example.skillhive.utils.SessionManager;

import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyOrdersActivity extends AppCompatActivity {

    private LinearLayout ordersContainer;
    private ProgressBar progressBarOrders;
    private TextView tvNoOrders;

    private OrdersApi ordersApi;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_my_orders);

        ordersContainer = findViewById(R.id.ordersContainer);
        progressBarOrders = findViewById(R.id.progressBarOrders);
        tvNoOrders = findViewById(R.id.tvNoOrders);

        ordersApi = RetrofitClient
                .getInstance()
                .create(OrdersApi.class);

        sessionManager = new SessionManager(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (ordersApi != null && sessionManager != null) {
            loadMyOrders();
        }
    }

    // =========================================================
    // LOAD ORDERS
    // =========================================================

    private void loadMyOrders() {

        String token = sessionManager.getToken();

        if (token == null || token.isEmpty()) {

            Toast.makeText(
                    this,
                    "Please login again",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        progressBarOrders.setVisibility(View.VISIBLE);
        tvNoOrders.setVisibility(View.GONE);

        ordersApi
                .getMyOrders("Bearer " + token)
                .enqueue(new Callback<List<Order>>() {

                    @Override
                    public void onResponse(
                            Call<List<Order>> call,
                            Response<List<Order>> response) {

                        progressBarOrders.setVisibility(View.GONE);

                        if (response.isSuccessful()
                                && response.body() != null) {

                            List<Order> orders = response.body();

                            if (orders.isEmpty()) {

                                ordersContainer.removeAllViews();
                                tvNoOrders.setVisibility(View.VISIBLE);

                            } else {

                                tvNoOrders.setVisibility(View.GONE);
                                displayOrders(orders);
                            }

                        } else {

                            Toast.makeText(
                                    MyOrdersActivity.this,
                                    "Failed to load orders: "
                                            + response.code(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<List<Order>> call,
                            Throwable t) {

                        progressBarOrders.setVisibility(View.GONE);

                        Toast.makeText(
                                MyOrdersActivity.this,
                                "Connection failed: "
                                        + t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    // =========================================================
    // DISPLAY ORDERS
    // =========================================================

    private void displayOrders(List<Order> orders) {

        ordersContainer.removeAllViews();

        for (Order order : orders) {

            if (order == null) {
                continue;
            }

            // =================================================
            // ORDER CARD
            // =================================================

            LinearLayout card = new LinearLayout(this);

            card.setOrientation(LinearLayout.VERTICAL);

            card.setPadding(
                    18,
                    18,
                    18,
                    18
            );

            card.setBackgroundResource(
                    R.drawable.bg_order_card
            );

            LinearLayout.LayoutParams cardParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

            cardParams.setMargins(
                    0,
                    0,
                    0,
                    16
            );

            card.setLayoutParams(cardParams);

            // =================================================
            // PROJECT TITLE
            // =================================================

            String projectTitle =
                    getSafeText(
                            order.getProjectTitle(),
                            "Project Request"
                    );

            TextView projectTitleView =
                    createTextView(
                            projectTitle,
                            20,
                            "#0F172A",
                            true
                    );

            card.addView(projectTitleView);

            // =================================================
            // SERVICE NAME
            // =================================================

            String serviceTitle = "Service";

            if (order.getService() != null) {

                serviceTitle =
                        getSafeText(
                                order.getService().getTitle(),
                                "Service"
                        );
            }

            TextView serviceView =
                    createTextView(
                            serviceTitle,
                            14,
                            "#1264E5",
                            true
                    );

            addTopMargin(
                    serviceView,
                    6
            );

            card.addView(serviceView);

            // =================================================
            // PROVIDER
            // =================================================

            String providerName = "Unknown";

            if (order.getProvider() != null) {

                providerName =
                        getSafeText(
                                order.getProvider().getFullName(),
                                "Unknown"
                        );
            }

            TextView providerView =
                    createTextView(
                            "Provider  •  " + providerName,
                            13,
                            "#64748B",
                            false
                    );

            addTopMargin(
                    providerView,
                    10
            );

            card.addView(providerView);

            // =================================================
            // DIVIDER
            // =================================================

            View divider = new View(this);

            divider.setBackgroundColor(
                    Color.parseColor("#E2E8F0")
            );

            LinearLayout.LayoutParams dividerParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            1
                    );

            dividerParams.setMargins(
                    0,
                    14,
                    0,
                    14
            );

            divider.setLayoutParams(dividerParams);

            card.addView(divider);

            // =================================================
            // REQUIREMENTS
            // =================================================

            TextView requirementsLabel =
                    createTextView(
                            "PROJECT REQUIREMENTS",
                            11,
                            "#64748B",
                            true
                    );

            card.addView(requirementsLabel);

            String requirements =
                    getSafeText(
                            order.getRequirements(),
                            "No requirements provided."
                    );

            TextView requirementsView =
                    createTextView(
                            requirements,
                            14,
                            "#334155",
                            false
                    );

            requirementsView.setLineSpacing(
                    2,
                    1.0f
            );

            addTopMargin(
                    requirementsView,
                    5
            );

            card.addView(requirementsView);

            // =================================================
            // DEADLINE
            // =================================================

            TextView deadlineView =
                    createTextView(
                            "Deadline: "
                                    + getSafeText(
                                    order.getDeadline(),
                                    "Not specified"
                            ),
                            13,
                            "#64748B",
                            false
                    );

            addTopMargin(
                    deadlineView,
                    12
            );

            card.addView(deadlineView);

            // =================================================
            // PRICE
            // =================================================

            String priceText;

            if (order.getPrice() != null) {

                priceText =
                        String.format(
                                Locale.US,
                                "₹%.2f",
                                order.getPrice()
                        );

            } else {

                priceText = "Price unavailable";
            }

            TextView priceView =
                    createTextView(
                            priceText,
                            19,
                            "#0F172A",
                            true
                    );

            addTopMargin(
                    priceView,
                    14
            );

            card.addView(priceView);

            // =================================================
            // PAYMENT LABEL
            // =================================================

            TextView paymentView =
                    createTextView(
                            "Service Amount",
                            11,
                            "#64748B",
                            false
                    );

            card.addView(paymentView);

            // =================================================
            // STATUS
            // =================================================

            String status =
                    getSafeText(
                            order.getStatus(),
                            "UNKNOWN"
                    ).toUpperCase(Locale.US);

            TextView statusView =
                    createStatusBadge(status);

            addTopMargin(
                    statusView,
                    14
            );

            card.addView(statusView);

            // =================================================
            // STATUS MESSAGE
            // =================================================

            String statusMessage =
                    getStatusMessage(status);

            TextView statusMessageView =
                    createTextView(
                            statusMessage,
                            13,
                            "#64748B",
                            false
                    );

            addTopMargin(
                    statusMessageView,
                    7
            );

            card.addView(statusMessageView);

            // =================================================
            // SUBMITTED → VIEW SUBMITTED PROJECT
            // =================================================

            if ("SUBMITTED".equals(status)) {

                AppCompatButton btnViewProject =
                        createPrimaryButton(
                                "View Submitted Project"
                        );

                addTopMargin(
                        btnViewProject,
                        16
                );

                card.addView(btnViewProject);

                btnViewProject.setOnClickListener(v -> {

                    Intent intent =
                            new Intent(
                                    MyOrdersActivity.this,
                                    SubmittedProjectActivity.class
                            );

                    intent.putExtra(
                            "orderId",
                            order.getId()
                    );

                    startActivity(intent);
                });
            }

            // =================================================
            // COMPLETED → REVIEW
            // =================================================

            if ("COMPLETED".equals(status)) {

                AppCompatButton btnReview =
                        createSecondaryButton(
                                "Review Service"
                        );

                addTopMargin(
                        btnReview,
                        16
                );

                card.addView(btnReview);

                btnReview.setOnClickListener(v -> {

                    Intent intent =
                            new Intent(
                                    MyOrdersActivity.this,
                                    ReviewActivity.class
                            );

                    intent.putExtra(
                            "orderId",
                            order.getId()
                    );

                    startActivity(intent);
                });
            }

            // =================================================
            // ADD CARD
            // =================================================

            ordersContainer.addView(card);
        }
    }

    // =========================================================
    // STATUS BADGE
    // =========================================================

    private TextView createStatusBadge(String status) {

        TextView statusView =
                createTextView(
                        getStatusDisplayText(status),
                        12,
                        getStatusTextColor(status),
                        true
                );

        statusView.setGravity(
                Gravity.CENTER
        );

        statusView.setPadding(
                13,
                8,
                13,
                8
        );

        GradientDrawable background =
                new GradientDrawable();

        background.setColor(
                Color.parseColor(
                        getStatusBackgroundColor(status)
                )
        );

        background.setCornerRadius(
                50
        );

        statusView.setBackground(background);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        statusView.setLayoutParams(params);

        return statusView;
    }

    // =========================================================
    // STATUS DISPLAY TEXT
    // =========================================================

    private String getStatusDisplayText(String status) {

        switch (status) {

            case "REQUESTED":
                return "●  REQUESTED";

            case "ACCEPTED":
                return "●  ACCEPTED";

            case "IN_PROGRESS":
                return "●  IN PROGRESS";

            case "SUBMITTED":
                return "●  PROJECT SUBMITTED";

            case "COMPLETED":
                return "●  COMPLETED";

            case "REJECTED":
                return "●  REJECTED";

            case "REVISION_REQUESTED":
                return "●  REVISION REQUESTED";

            default:
                return "●  " + status;
        }
    }

    // =========================================================
    // STATUS MESSAGE
    // =========================================================

    private String getStatusMessage(String status) {

        switch (status) {

            case "REQUESTED":
                return "Waiting for the provider to accept your request.";

            case "ACCEPTED":
                return "Your request has been accepted by the provider.";

            case "IN_PROGRESS":
                return "The provider is currently working on your project.";

            case "SUBMITTED":
                return "The provider has submitted the project. Review it before accepting.";

            case "COMPLETED":
                return "Order completed successfully. You can now review the service.";

            case "REJECTED":
                return "The provider rejected this request.";

            case "REVISION_REQUESTED":
                return "Changes have been requested from the provider.";

            default:
                return "Order status: " + status;
        }
    }

    // =========================================================
    // STATUS TEXT COLORS
    // =========================================================

    private String getStatusTextColor(String status) {

        switch (status) {

            case "REQUESTED":
                return "#B45309";

            case "ACCEPTED":
                return "#0369A1";

            case "IN_PROGRESS":
                return "#1264E5";

            case "SUBMITTED":
                return "#7C3AED";

            case "COMPLETED":
                return "#15803D";

            case "REJECTED":
                return "#DC2626";

            case "REVISION_REQUESTED":
                return "#C2410C";

            default:
                return "#64748B";
        }
    }

    // =========================================================
    // STATUS BACKGROUND COLORS
    // =========================================================

    private String getStatusBackgroundColor(String status) {

        switch (status) {

            case "REQUESTED":
                return "#FEF3C7";

            case "ACCEPTED":
                return "#E0F2FE";

            case "IN_PROGRESS":
                return "#E8F1FF";

            case "SUBMITTED":
                return "#F3E8FF";

            case "COMPLETED":
                return "#DCFCE7";

            case "REJECTED":
                return "#FEE2E2";

            case "REVISION_REQUESTED":
                return "#FFEDD5";

            default:
                return "#F1F5F9";
        }
    }

    // =========================================================
    // CREATE TEXT VIEW
    // =========================================================

    private TextView createTextView(
            String text,
            float textSize,
            String textColor,
            boolean bold) {

        TextView textView =
                new TextView(this);

        textView.setText(text);

        textView.setTextSize(textSize);

        textView.setTextColor(
                Color.parseColor(textColor)
        );

        if (bold) {

            textView.setTypeface(
                    null,
                    Typeface.BOLD
            );
        }

        textView.setLayoutParams(
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        return textView;
    }

    // =========================================================
    // PRIMARY BUTTON
    // =========================================================

    private AppCompatButton createPrimaryButton(
            String text) {

        AppCompatButton button =
                new AppCompatButton(this);

        button.setText(text);

        button.setTextSize(14);

        button.setTextColor(
                Color.WHITE
        );

        button.setAllCaps(false);

        button.setBackgroundResource(
                R.drawable.bg_primary_button
        );

        button.setPadding(
                10,
                0,
                10,
                0
        );

        button.setMinHeight(0);

        button.setStateListAnimator(null);

        button.setLayoutParams(
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        52
                )
        );

        return button;
    }

    // =========================================================
    // SECONDARY BUTTON
    // =========================================================

    private AppCompatButton createSecondaryButton(
            String text) {

        AppCompatButton button =
                new AppCompatButton(this);

        button.setText(text);

        button.setTextSize(14);

        button.setTextColor(
                Color.parseColor("#1264E5")
        );

        button.setAllCaps(false);

        button.setBackgroundResource(
                R.drawable.bg_secondary_button
        );

        button.setPadding(
                10,
                0,
                10,
                0
        );

        button.setMinHeight(0);

        button.setStateListAnimator(null);

        button.setLayoutParams(
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        52
                )
        );

        return button;
    }

    // =========================================================
    // TOP MARGIN
    // =========================================================

    private void addTopMargin(
            View view,
            int margin) {

        if (view.getLayoutParams()
                instanceof LinearLayout.LayoutParams) {

            LinearLayout.LayoutParams params =
                    (LinearLayout.LayoutParams)
                            view.getLayoutParams();

            params.setMargins(
                    0,
                    margin,
                    0,
                    0
            );

            view.setLayoutParams(params);
        }
    }

    // =========================================================
    // SAFE TEXT
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