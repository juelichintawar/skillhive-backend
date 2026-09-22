package com.example.skillhive;

import android.content.Intent;
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

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyReceivedOrdersActivity extends AppCompatActivity {

    private LinearLayout ordersContainer;
    private ProgressBar progressBarOrders;
    private TextView tvNoOrders;

    private OrdersApi ordersApi;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_received_orders);

        ordersContainer =
                findViewById(R.id.ordersContainer);

        progressBarOrders =
                findViewById(R.id.progressBarOrders);

        tvNoOrders =
                findViewById(R.id.tvNoOrders);

        ordersApi =
                RetrofitClient
                        .getInstance()
                        .create(OrdersApi.class);

        sessionManager =
                new SessionManager(this);

        loadReceivedOrders();
    }

    // =========================================================
    // LOAD RECEIVED ORDERS
    // =========================================================

    private void loadReceivedOrders() {

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

        progressBarOrders.setVisibility(
                View.VISIBLE
        );

        tvNoOrders.setVisibility(
                View.GONE
        );

        ordersApi
                .getReceivedOrders(
                        "Bearer " + token
                )
                .enqueue(
                        new Callback<List<Order>>() {

                            @Override
                            public void onResponse(
                                    Call<List<Order>> call,
                                    Response<List<Order>> response) {

                                progressBarOrders
                                        .setVisibility(
                                                View.GONE
                                        );

                                if (response.isSuccessful()
                                        && response.body() != null) {

                                    List<Order> orders =
                                            response.body();

                                    if (orders.isEmpty()) {

                                        ordersContainer
                                                .removeAllViews();

                                        tvNoOrders
                                                .setVisibility(
                                                        View.VISIBLE
                                                );

                                    } else {

                                        displayOrders(
                                                orders
                                        );
                                    }

                                } else {

                                    Toast.makeText(
                                            MyReceivedOrdersActivity.this,
                                            "Failed to load received orders: "
                                                    + response.code(),
                                            Toast.LENGTH_LONG
                                    ).show();
                                }
                            }

                            @Override
                            public void onFailure(
                                    Call<List<Order>> call,
                                    Throwable t) {

                                progressBarOrders
                                        .setVisibility(
                                                View.GONE
                                        );

                                Toast.makeText(
                                        MyReceivedOrdersActivity.this,
                                        "Connection failed: "
                                                + t.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }
                );
    }

    // =========================================================
    // DISPLAY ORDERS
    // =========================================================

    private void displayOrders(
            List<Order> orders) {

        ordersContainer.removeAllViews();

        for (Order order : orders) {

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

            card.setBackgroundResource(
                    R.drawable.bg_order_card
            );

            // -------------------------------------------------
            // SERVICE NAME
            // -------------------------------------------------

            String serviceTitle =
                    "Service";

            if (order.getService() != null
                    && order.getService().getTitle() != null
                    && !order.getService()
                    .getTitle()
                    .trim()
                    .isEmpty()) {

                serviceTitle =
                        order.getService().getTitle();
            }

            TextView serviceTitleView =
                    new TextView(this);

            serviceTitleView.setText(
                    serviceTitle
            );

            serviceTitleView.setTextSize(
                    19
            );

            serviceTitleView.setTextColor(
                    0xFF1264E5
            );

            serviceTitleView.setTypeface(
                    null,
                    Typeface.BOLD
            );

            card.addView(
                    serviceTitleView
            );

            // -------------------------------------------------
            // CLIENT NAME
            // -------------------------------------------------

            String clientName =
                    "Unknown Client";

            if (order.getClient() != null
                    && order.getClient().getFullName() != null
                    && !order.getClient()
                    .getFullName()
                    .trim()
                    .isEmpty()) {

                clientName =
                        order.getClient().getFullName();
            }

            TextView clientView =
                    new TextView(this);

            clientView.setText(
                    "Client  •  " + clientName
            );

            clientView.setTextSize(
                    14
            );

            clientView.setTextColor(
                    0xFF475569
            );

            LinearLayout.LayoutParams clientParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

            clientParams.setMargins(
                    0,
                    10,
                    0,
                    0
            );

            clientView.setLayoutParams(
                    clientParams
            );

            card.addView(
                    clientView
            );

            // -------------------------------------------------
            // PRICE
            // -------------------------------------------------

            String price;

            if (order.getPrice() != null) {

                price = String.format(
                        Locale.US,
                        "₹%.2f",
                        order.getPrice()
                );

            } else {

                price =
                        "Price not available";
            }

            TextView priceView =
                    new TextView(this);

            priceView.setText(
                    price
            );

            priceView.setTextSize(
                    18
            );

            priceView.setTextColor(
                    0xFF0F172A
            );

            priceView.setTypeface(
                    null,
                    Typeface.BOLD
            );

            LinearLayout.LayoutParams priceParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

            priceParams.setMargins(
                    0,
                    12,
                    0,
                    0
            );

            priceView.setLayoutParams(
                    priceParams
            );

            card.addView(
                    priceView
            );

            // -------------------------------------------------
            // PROJECT TITLE
            // -------------------------------------------------

            if (order.getProjectTitle() != null
                    && !order.getProjectTitle()
                    .trim()
                    .isEmpty()) {

                TextView projectTitleView =
                        new TextView(this);

                projectTitleView.setText(
                        "Project: "
                                + order.getProjectTitle()
                );

                projectTitleView.setTextSize(
                        14
                );

                projectTitleView.setTextColor(
                        0xFF334155
                );

                LinearLayout.LayoutParams params =
                        new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );

                params.setMargins(
                        0,
                        8,
                        0,
                        0
                );

                projectTitleView.setLayoutParams(
                        params
                );

                card.addView(
                        projectTitleView
                );
            }

            // -------------------------------------------------
            // DEADLINE
            // -------------------------------------------------

            if (order.getDeadline() != null
                    && !order.getDeadline()
                    .trim()
                    .isEmpty()) {

                TextView deadlineView =
                        new TextView(this);

                deadlineView.setText(
                        "Deadline: "
                                + order.getDeadline()
                );

                deadlineView.setTextSize(
                        14
                );

                deadlineView.setTextColor(
                        0xFF475569
                );

                LinearLayout.LayoutParams params =
                        new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );

                params.setMargins(
                        0,
                        6,
                        0,
                        0
                );

                deadlineView.setLayoutParams(
                        params
                );

                card.addView(
                        deadlineView
                );
            }

            // -------------------------------------------------
            // STATUS
            // -------------------------------------------------

            String status =
                    order.getStatus();

            if (status == null
                    || status.trim().isEmpty()) {

                status =
                        "UNKNOWN";
            }

            TextView statusView =
                    createStatusView(
                            status
                    );

            LinearLayout.LayoutParams statusParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

            statusParams.setMargins(
                    0,
                    14,
                    0,
                    0
            );

            statusView.setLayoutParams(
                    statusParams
            );

            card.addView(
                    statusView
            );

            // -------------------------------------------------
            // REVISION REQUEST
            // -------------------------------------------------

            if ("REVISION_REQUESTED".equals(
                    status)) {

                createRevisionRequestSection(
                        card,
                        order
                );
            }

            // -------------------------------------------------
            // ACTION BUTTON
            // -------------------------------------------------

            switch (status) {

                case "REQUESTED":

                    createRequestedButtons(
                            card,
                            order
                    );

                    break;

                case "ACCEPTED":

                    createStartWorkButton(
                            card,
                            order
                    );

                    break;

                case "REVISION_REQUESTED":

                    createStartWorkButton(
                            card,
                            order
                    );

                    break;

                case "IN_PROGRESS":

                    createSubmitWorkButton(
                            card,
                            order
                    );

                    break;

                case "SUBMITTED":

                    createInfoButton(
                            card,
                            "Waiting for Client Review"
                    );

                    break;

                case "COMPLETED":

                    createInfoButton(
                            card,
                            "Order Completed"
                    );

                    break;

                case "REJECTED":

                    createInfoButton(
                            card,
                            "Order Rejected"
                    );

                    break;

                default:

                    createInfoButton(
                            card,
                            "No Action Available"
                    );

                    break;
            }

            // -------------------------------------------------
            // CARD MARGIN
            // -------------------------------------------------

            LinearLayout.LayoutParams cardParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

            cardParams.setMargins(
                    0,
                    0,
                    0,
                    14
            );

            card.setLayoutParams(
                    cardParams
            );

            ordersContainer.addView(
                    card
            );
        }
    }

    // =========================================================
    // STATUS VIEW
    // =========================================================

    private TextView createStatusView(
            String status) {

        TextView statusView =
                new TextView(this);

        String displayStatus;

        int backgroundColor;
        int textColor;

        switch (status) {

            case "REQUESTED":

                displayStatus =
                        "REQUESTED";

                backgroundColor =
                        0xFFFFEDD5;

                textColor =
                        0xFF9A3412;

                break;

            case "ACCEPTED":

                displayStatus =
                        "ACCEPTED";

                backgroundColor =
                        0xFFE8F0FE;

                textColor =
                        0xFF1264E5;

                break;

            case "IN_PROGRESS":

                displayStatus =
                        "IN PROGRESS";

                backgroundColor =
                        0xFFE8F0FE;

                textColor =
                        0xFF1264E5;

                break;

            case "REVISION_REQUESTED":

                displayStatus =
                        "REVISION REQUESTED";

                backgroundColor =
                        0xFFFFEDD5;

                textColor =
                        0xFF9A3412;

                break;

            case "SUBMITTED":

                displayStatus =
                        "SUBMITTED";

                backgroundColor =
                        0xFFE8F0FE;

                textColor =
                        0xFF1264E5;

                break;

            case "COMPLETED":

                displayStatus =
                        "COMPLETED";

                backgroundColor =
                        0xFFDCFCE7;

                textColor =
                        0xFF15803D;

                break;

            case "REJECTED":

                displayStatus =
                        "REJECTED";

                backgroundColor =
                        0xFFFEE2E2;

                textColor =
                        0xFFDC2626;

                break;

            default:

                displayStatus =
                        status;

                backgroundColor =
                        0xFFF1F5F9;

                textColor =
                        0xFF475569;

                break;
        }

        statusView.setText(
                displayStatus
        );

        statusView.setTextSize(
                12
        );

        statusView.setTypeface(
                null,
                Typeface.BOLD
        );

        statusView.setTextColor(
                textColor
        );

        statusView.setGravity(
                Gravity.CENTER
        );

        statusView.setPadding(
                14,
                7,
                14,
                7
        );

        GradientDrawable background =
                new GradientDrawable();

        background.setColor(
                backgroundColor
        );

        background.setCornerRadius(
                30f
        );

        statusView.setBackground(
                background
        );

        return statusView;
    }

    // =========================================================
    // REQUESTED
    // ACCEPT + REJECT
    // =========================================================

    private void createRequestedButtons(
            LinearLayout card,
            Order order) {

        LinearLayout buttonRow =
                new LinearLayout(this);

        buttonRow.setOrientation(
                LinearLayout.HORIZONTAL
        );

        buttonRow.setGravity(
                Gravity.CENTER
        );

        // -----------------------------------------------------
        // ACCEPT
        // -----------------------------------------------------

        AppCompatButton btnAccept =
                createActionButton(
                        "Accept Order",
                        0xFF1264E5,
                        0xFFFFFFFF
                );

        // -----------------------------------------------------
        // REJECT
        // -----------------------------------------------------

        AppCompatButton btnReject =
                createActionButton(
                        "Reject Order",
                        0xFFE8F0FE,
                        0xFF1264E5
                );

        // Border for reject button
        GradientDrawable rejectBackground =
                new GradientDrawable();

        rejectBackground.setColor(
                0xFFE8F0FE
        );

        rejectBackground.setStroke(
                2,
                0xFF1264E5
        );

        rejectBackground.setCornerRadius(
                28f
        );

        btnReject.setBackground(
                rejectBackground
        );

        btnReject.setBackgroundTintList(
                null
        );

        // -----------------------------------------------------
        // LAYOUT
        // -----------------------------------------------------

        LinearLayout.LayoutParams acceptParams =
                new LinearLayout.LayoutParams(
                        0,
                        58,
                        1
                );

        acceptParams.setMargins(
                0,
                16,
                6,
                0
        );

        LinearLayout.LayoutParams rejectParams =
                new LinearLayout.LayoutParams(
                        0,
                        58,
                        1
                );

        rejectParams.setMargins(
                6,
                16,
                0,
                0
        );

        btnAccept.setLayoutParams(
                acceptParams
        );

        btnReject.setLayoutParams(
                rejectParams
        );

        buttonRow.addView(
                btnAccept
        );

        buttonRow.addView(
                btnReject
        );

        card.addView(
                buttonRow
        );

        // -----------------------------------------------------
        // ACCEPT CLICK
        // -----------------------------------------------------

        btnAccept.setOnClickListener(
                v -> updateOrderStatus(
                        order,
                        "ACCEPTED",
                        btnAccept,
                        btnReject
                )
        );

        // -----------------------------------------------------
        // REJECT CLICK
        // -----------------------------------------------------

        btnReject.setOnClickListener(
                v -> updateOrderStatus(
                        order,
                        "REJECTED",
                        btnAccept,
                        btnReject
                )
        );
    }

    // =========================================================
    // START WORK
    // =========================================================

    private void createStartWorkButton(
            LinearLayout card,
            Order order) {

        String buttonText;

        if ("REVISION_REQUESTED".equals(
                order.getStatus())) {

            buttonText =
                    "Start Revised Work  →";

        } else {

            buttonText =
                    "Start Work  →";
        }

        AppCompatButton btnStartWork =
                createActionButton(
                        buttonText,
                        0xFF1264E5,
                        0xFFFFFFFF
                );

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        58
                );

        params.setMargins(
                0,
                16,
                0,
                0
        );

        btnStartWork.setLayoutParams(
                params
        );

        card.addView(
                btnStartWork
        );

        btnStartWork.setOnClickListener(
                v -> {

                    btnStartWork.setText(
                            "Starting Work..."
                    );

                    btnStartWork.setEnabled(
                            false
                    );

                    startWork(
                            order,
                            btnStartWork
                    );
                }
        );
    }

    // =========================================================
    // SUBMIT PROJECT
    // =========================================================

    private void createSubmitWorkButton(
            LinearLayout card,
            Order order) {

        AppCompatButton btnSubmit =
                createActionButton(
                        "Submit Project  →",
                        0xFF1264E5,
                        0xFFFFFFFF
                );

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        58
                );

        params.setMargins(
                0,
                16,
                0,
                0
        );

        btnSubmit.setLayoutParams(
                params
        );

        card.addView(
                btnSubmit
        );

        btnSubmit.setOnClickListener(
                v -> {

                    Intent intent =
                            new Intent(
                                    MyReceivedOrdersActivity.this,
                                    SubmitProjectActivity.class
                            );

                    intent.putExtra(
                            "orderId",
                            order.getId()
                    );

                    startActivity(
                            intent
                    );
                }
        );
    }

    // =========================================================
    // GENERIC ACTION BUTTON
    // =========================================================

    private AppCompatButton createActionButton(
            String text,
            int backgroundColor,
            int textColor) {

        AppCompatButton button =
                new AppCompatButton(this);

        button.setText(
                text
        );

        button.setTextSize(
                15
        );

        button.setTextColor(
                textColor
        );

        button.setTypeface(
                null,
                Typeface.BOLD
        );

        button.setGravity(
                Gravity.CENTER
        );

        button.setAllCaps(
                false
        );

        button.setVisibility(
                View.VISIBLE
        );

        button.setEnabled(
                true
        );

        button.setAlpha(
                1.0f
        );

        button.setPadding(
                10,
                0,
                10,
                0
        );

        GradientDrawable background =
                new GradientDrawable();

        background.setColor(
                backgroundColor
        );

        background.setCornerRadius(
                28f
        );

        button.setBackground(
                background
        );

        button.setBackgroundTintList(
                null
        );

        return button;
    }

    // =========================================================
    // INFO BUTTON
    // =========================================================

    private void createInfoButton(
            LinearLayout card,
            String text) {

        AppCompatButton button =
                createActionButton(
                        text,
                        0xFFF1F5F9,
                        0xFF475569
                );

        button.setEnabled(
                false
        );

        button.setAlpha(
                1.0f
        );

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        54
                );

        params.setMargins(
                0,
                16,
                0,
                0
        );

        button.setLayoutParams(
                params
        );

        card.addView(
                button
        );
    }

    // =========================================================
    // REVISION REQUEST
    // =========================================================

    private void createRevisionRequestSection(
            LinearLayout card,
            Order order) {

        LinearLayout revisionBox =
                new LinearLayout(this);

        revisionBox.setOrientation(
                LinearLayout.VERTICAL
        );

        revisionBox.setPadding(
                16,
                16,
                16,
                16
        );

        GradientDrawable revisionBackground =
                new GradientDrawable();

        revisionBackground.setColor(
                0xFFFFF7ED
        );

        revisionBackground.setCornerRadius(
                18f
        );

        revisionBox.setBackground(
                revisionBackground
        );

        LinearLayout.LayoutParams boxParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        boxParams.setMargins(
                0,
                16,
                0,
                0
        );

        revisionBox.setLayoutParams(
                boxParams
        );

        // -----------------------------------------------------
        // TITLE
        // -----------------------------------------------------

        TextView title =
                new TextView(this);

        title.setText(
                "Client Requested Changes"
        );

        title.setTextSize(
                16
        );

        title.setTextColor(
                0xFF9A3412
        );

        title.setTypeface(
                null,
                Typeface.BOLD
        );

        revisionBox.addView(
                title
        );

        // -----------------------------------------------------
        // MESSAGE
        // -----------------------------------------------------

        TextView message =
                new TextView(this);

        String revisionMessage =
                order.getRevisionMessage();

        if (revisionMessage == null
                || revisionMessage.trim().isEmpty()) {

            revisionMessage =
                    "No revision details were provided.";
        }

        message.setText(
                revisionMessage
        );

        message.setTextSize(
                14
        );

        message.setTextColor(
                0xFF334155
        );

        message.setLineSpacing(
                0,
                1.2f
        );

        LinearLayout.LayoutParams messageParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        messageParams.setMargins(
                0,
                10,
                0,
                0
        );

        message.setLayoutParams(
                messageParams
        );

        revisionBox.addView(
                message
        );

        card.addView(
                revisionBox
        );
    }

    // =========================================================
    // UPDATE ORDER STATUS
    // =========================================================

    private void updateOrderStatus(
            Order order,
            String newStatus,
            AppCompatButton btnAccept,
            AppCompatButton btnReject) {

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

        btnAccept.setEnabled(
                false
        );

        btnReject.setEnabled(
                false
        );

        if ("ACCEPTED".equals(
                newStatus)) {

            btnAccept.setText(
                    "Accepting..."
            );

        } else {

            btnReject.setText(
                    "Rejecting..."
            );
        }

        ordersApi.updateOrderStatus(
                order.getId(),
                newStatus,
                null,
                null,
                "Bearer " + token
        ).enqueue(
                new Callback<ResponseBody>() {

                    @Override
                    public void onResponse(
                            Call<ResponseBody> call,
                            Response<ResponseBody> response) {

                        if (response.isSuccessful()) {

                            Toast.makeText(
                                    MyReceivedOrdersActivity.this,
                                    "Order "
                                            + newStatus
                                            .toLowerCase(Locale.US)
                                            + " successfully",
                                    Toast.LENGTH_LONG
                            ).show();

                            loadReceivedOrders();

                        } else {

                            btnAccept.setEnabled(
                                    true
                            );

                            btnReject.setEnabled(
                                    true
                            );

                            btnAccept.setText(
                                    "Accept Order"
                            );

                            btnReject.setText(
                                    "Reject Order"
                            );

                            Toast.makeText(
                                    MyReceivedOrdersActivity.this,
                                    "Failed to update order: "
                                            + response.code(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<ResponseBody> call,
                            Throwable t) {

                        btnAccept.setEnabled(
                                true
                        );

                        btnReject.setEnabled(
                                true
                        );

                        btnAccept.setText(
                                "Accept Order"
                        );

                        btnReject.setText(
                                "Reject Order"
                        );

                        Toast.makeText(
                                MyReceivedOrdersActivity.this,
                                "Connection failed: "
                                        + t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    // =========================================================
    // START WORK
    // =========================================================

    private void startWork(
            Order order,
            AppCompatButton btnStartWork) {

        String token =
                sessionManager.getToken();

        if (token == null
                || token.isEmpty()) {

            btnStartWork.setEnabled(
                    true
            );

            btnStartWork.setText(
                    "Start Work  →"
            );

            Toast.makeText(
                    this,
                    "Please login again",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        ordersApi.updateOrderStatus(
                order.getId(),
                "IN_PROGRESS",
                null,
                null,
                "Bearer " + token
        ).enqueue(
                new Callback<ResponseBody>() {

                    @Override
                    public void onResponse(
                            Call<ResponseBody> call,
                            Response<ResponseBody> response) {

                        if (response.isSuccessful()) {

                            Toast.makeText(
                                    MyReceivedOrdersActivity.this,
                                    "Work started successfully!",
                                    Toast.LENGTH_LONG
                            ).show();

                            loadReceivedOrders();

                        } else {

                            btnStartWork.setEnabled(
                                    true
                            );

                            if ("REVISION_REQUESTED".equals(
                                    order.getStatus())) {

                                btnStartWork.setText(
                                        "Start Revised Work  →"
                                );

                            } else {

                                btnStartWork.setText(
                                        "Start Work  →"
                                );
                            }

                            Toast.makeText(
                                    MyReceivedOrdersActivity.this,
                                    "Failed to start work: "
                                            + response.code(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<ResponseBody> call,
                            Throwable t) {

                        btnStartWork.setEnabled(
                                true
                        );

                        if ("REVISION_REQUESTED".equals(
                                order.getStatus())) {

                            btnStartWork.setText(
                                    "Start Revised Work  →"
                            );

                        } else {

                            btnStartWork.setText(
                                    "Start Work  →"
                            );
                        }

                        Toast.makeText(
                                MyReceivedOrdersActivity.this,
                                "Connection failed: "
                                        + t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }
}