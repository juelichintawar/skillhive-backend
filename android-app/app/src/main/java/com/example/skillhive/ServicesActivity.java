package com.example.skillhive;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.skillhive.model.Service;
import com.example.skillhive.network.RetrofitClient;
import com.example.skillhive.network.ServicesApi;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ServicesActivity extends AppCompatActivity {

    private EditText etSearchService;
    private LinearLayout servicesContainer;
    private ProgressBar progressBarServices;
    private TextView tvNoServices;

    private AppCompatButton btnCategoryAll;
    private AppCompatButton btnCategoryAndroid;
    private AppCompatButton btnCategoryWeb;
    private AppCompatButton btnCategoryUIUX;
    private AppCompatButton btnCategoryAI;
    private AppCompatButton btnCategoryDatabase;
    private AppCompatButton btnCategoryOther;

    private ServicesApi servicesApi;

    private List<Service> allServices = new ArrayList<>();

    private String selectedCategory = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_services);

        // -----------------------------
        // FIND VIEWS
        // -----------------------------

        etSearchService = findViewById(R.id.etSearchService);
        servicesContainer = findViewById(R.id.servicesContainer);
        progressBarServices = findViewById(R.id.progressBarServices);
        tvNoServices = findViewById(R.id.tvNoServices);

        btnCategoryAll = findViewById(R.id.btnCategoryAll);
        btnCategoryAndroid = findViewById(R.id.btnCategoryAndroid);
        btnCategoryWeb = findViewById(R.id.btnCategoryWeb);
        btnCategoryUIUX = findViewById(R.id.btnCategoryUIUX);
        btnCategoryAI = findViewById(R.id.btnCategoryAI);
        btnCategoryDatabase = findViewById(R.id.btnCategoryDatabase);
        btnCategoryOther = findViewById(R.id.btnCategoryOther);

        servicesApi = RetrofitClient
                .getInstance()
                .create(ServicesApi.class);

        // -----------------------------
        // SETUP
        // -----------------------------

        setupSearch();
        setupCategoryButtons();

        // -----------------------------
        // LOAD SERVICES
        // -----------------------------

        loadServices();
    }

    // =========================================================
    // SEARCH
    // =========================================================

    private void setupSearch() {

        etSearchService.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count) {

                        filterServices(
                                s.toString().trim()
                        );
                    }

                    @Override
                    public void afterTextChanged(
                            Editable s) {
                    }
                }
        );
    }

    // =========================================================
    // CATEGORY BUTTONS
    // =========================================================

    private void setupCategoryButtons() {

        btnCategoryAll.setOnClickListener(v ->
                selectCategory(
                        "All",
                        btnCategoryAll
                )
        );

        btnCategoryAndroid.setOnClickListener(v ->
                selectCategory(
                        "Android",
                        btnCategoryAndroid
                )
        );

        btnCategoryWeb.setOnClickListener(v ->
                selectCategory(
                        "Web",
                        btnCategoryWeb
                )
        );

        btnCategoryUIUX.setOnClickListener(v ->
                selectCategory(
                        "UI/UX",
                        btnCategoryUIUX
                )
        );

        btnCategoryAI.setOnClickListener(v ->
                selectCategory(
                        "AI",
                        btnCategoryAI
                )
        );

        btnCategoryDatabase.setOnClickListener(v ->
                selectCategory(
                        "Database",
                        btnCategoryDatabase
                )
        );

        btnCategoryOther.setOnClickListener(v ->
                selectCategory(
                        "Other",
                        btnCategoryOther
                )
        );
    }

    // =========================================================
    // SELECT CATEGORY
    // =========================================================

    private void selectCategory(
            String category,
            AppCompatButton selectedButton) {

        selectedCategory = category;

        resetCategoryButtons();

        selectedButton.setBackgroundResource(
                R.drawable.bg_category_selected
        );

        selectedButton.setTextColor(
                Color.WHITE
        );

        filterServices(
                etSearchService
                        .getText()
                        .toString()
                        .trim()
        );
    }

    // =========================================================
    // RESET CATEGORY BUTTONS
    // =========================================================

    private void resetCategoryButtons() {

        AppCompatButton[] buttons = {
                btnCategoryAll,
                btnCategoryAndroid,
                btnCategoryWeb,
                btnCategoryUIUX,
                btnCategoryAI,
                btnCategoryDatabase,
                btnCategoryOther
        };

        for (AppCompatButton button : buttons) {

            button.setBackgroundResource(
                    R.drawable.bg_category
            );

            button.setTextColor(
                    Color.rgb(18, 100, 229)
            );
        }
    }

    // =========================================================
    // LOAD SERVICES
    // =========================================================

    private void loadServices() {

        progressBarServices.setVisibility(
                View.VISIBLE
        );

        tvNoServices.setVisibility(
                View.GONE
        );

        servicesApi.getAllServices()
                .enqueue(new Callback<List<Service>>() {

                    @Override
                    public void onResponse(
                            Call<List<Service>> call,
                            Response<List<Service>> response) {

                        progressBarServices.setVisibility(
                                View.GONE
                        );

                        if (response.isSuccessful()
                                && response.body() != null) {

                            allServices.clear();

                            allServices.addAll(
                                    response.body()
                            );

                            filterServices(
                                    etSearchService
                                            .getText()
                                            .toString()
                                            .trim()
                            );

                        } else {

                            servicesContainer
                                    .removeAllViews();

                            tvNoServices.setText(
                                    "Unable to load services."
                            );

                            tvNoServices.setVisibility(
                                    View.VISIBLE
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<List<Service>> call,
                            Throwable t) {

                        progressBarServices.setVisibility(
                                View.GONE
                        );

                        servicesContainer
                                .removeAllViews();

                        tvNoServices.setText(
                                "Unable to connect to server."
                        );

                        tvNoServices.setVisibility(
                                View.VISIBLE
                        );
                    }
                });
    }

    // =========================================================
    // FILTER SERVICES
    // =========================================================

    private void filterServices(
            String searchText) {

        List<Service> filteredServices =
                new ArrayList<>();

        String search =
                searchText.toLowerCase(Locale.ROOT);

        for (Service service : allServices) {

            String title =
                    service.getTitle() != null
                            ? service.getTitle()
                            : "";

            String description =
                    service.getDescription() != null
                            ? service.getDescription()
                            : "";

            String category =
                    service.getCategory() != null
                            ? service.getCategory()
                            : "";

            boolean matchesSearch = true;

            boolean matchesCategory = true;

            // -----------------------------------------
            // SEARCH
            // -----------------------------------------

            if (!search.isEmpty()) {

                matchesSearch =
                        title.toLowerCase(Locale.ROOT)
                                .contains(search)

                                || description
                                .toLowerCase(Locale.ROOT)
                                .contains(search)

                                || category
                                .toLowerCase(Locale.ROOT)
                                .contains(search);
            }

            // -----------------------------------------
            // CATEGORY
            // -----------------------------------------

            if (!selectedCategory.equals("All")) {

                matchesCategory =
                        matchesCategory(
                                category,
                                selectedCategory
                        );
            }

            // -----------------------------------------
            // ADD MATCHING SERVICE
            // -----------------------------------------

            if (matchesSearch && matchesCategory) {

                filteredServices.add(service);
            }
        }

        displayServices(
                filteredServices
        );
    }

    // =========================================================
    // FLEXIBLE CATEGORY MATCHING
    // =========================================================

    private boolean matchesCategory(
            String actualCategory,
            String selectedCategory) {

        String actual =
                actualCategory
                        .toLowerCase(Locale.ROOT)
                        .trim();

        String selected =
                selectedCategory
                        .toLowerCase(Locale.ROOT)
                        .trim();

        // Android
        if (selected.equals("android")) {

            return actual.contains("android");
        }

        // Web
        if (selected.equals("web")) {

            return actual.contains("web");
        }

        // UI/UX
        if (selected.equals("ui/ux")) {

            return actual.contains("ui")
                    || actual.contains("ux")
                    || actual.contains("design");
        }

        // AI / ML
        if (selected.equals("ai")) {

            return actual.contains("ai")
                    || actual.contains("ml")
                    || actual.contains("machine learning")
                    || actual.contains("artificial intelligence");
        }

        // Database
        if (selected.equals("database")) {

            return actual.contains("database")
                    || actual.contains("dbms")
                    || actual.contains("mysql")
                    || actual.contains("sql");
        }

        // Other
        if (selected.equals("other")) {

            return !actual.contains("android")
                    && !actual.contains("web")
                    && !actual.contains("ui")
                    && !actual.contains("ux")
                    && !actual.contains("design")
                    && !actual.contains("ai")
                    && !actual.contains("ml")
                    && !actual.contains("machine learning")
                    && !actual.contains("artificial intelligence")
                    && !actual.contains("database")
                    && !actual.contains("dbms")
                    && !actual.contains("mysql")
                    && !actual.contains("sql");
        }

        return actual.equals(selected);
    }

    // =========================================================
    // DISPLAY SERVICES
    // =========================================================

    private void displayServices(
            List<Service> services) {

        servicesContainer.removeAllViews();

        if (services.isEmpty()) {

            String searchText =
                    etSearchService
                            .getText()
                            .toString()
                            .trim();

            if (!searchText.isEmpty()
                    && !selectedCategory.equals("All")) {

                tvNoServices.setText(
                        "No services found for \""
                                + searchText
                                + "\" in "
                                + selectedCategory
                                + "."
                );

            } else if (!searchText.isEmpty()) {

                tvNoServices.setText(
                        "No services found for \""
                                + searchText
                                + "\"."
                );

            } else if (!selectedCategory.equals("All")) {

                tvNoServices.setText(
                        "No services available in "
                                + selectedCategory
                                + "."
                );

            } else {

                tvNoServices.setText(
                        "No services available."
                );
            }

            tvNoServices.setVisibility(
                    View.VISIBLE
            );

            return;
        }

        tvNoServices.setVisibility(
                View.GONE
        );

        // =====================================================
        // SERVICE CARDS
        // =====================================================

        for (Service service : services) {

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
                    R.drawable.bg_service_card
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
                    14
            );

            card.setLayoutParams(
                    cardParams
            );

            // -----------------------------------------
            // TITLE
            // -----------------------------------------

            TextView title =
                    new TextView(this);

            title.setText(
                    service.getTitle()
            );

            title.setTextColor(
                    Color.rgb(
                            18,
                            100,
                            229
                    )
            );

            title.setTextSize(18);

            title.setTypeface(
                    null,
                    Typeface.BOLD
            );

            card.addView(title);

            // -----------------------------------------
            // DESCRIPTION
            // -----------------------------------------

            TextView description =
                    new TextView(this);

            description.setText(
                    service.getDescription()
            );

            description.setTextColor(
                    Color.rgb(
                            71,
                            85,
                            105
                    )
            );

            description.setTextSize(14);

            LinearLayout.LayoutParams
                    descriptionParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

            descriptionParams.setMargins(
                    0,
                    8,
                    0,
                    0
            );

            description.setLayoutParams(
                    descriptionParams
            );

            card.addView(description);

            // -----------------------------------------
            // CATEGORY
            // -----------------------------------------

            TextView category =
                    new TextView(this);

            category.setText(
                    "Category: "
                            + service.getCategory()
            );

            category.setTextColor(
                    Color.rgb(
                            100,
                            116,
                            139
                    )
            );

            category.setTextSize(13);

            LinearLayout.LayoutParams
                    categoryParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

            categoryParams.setMargins(
                    0,
                    10,
                    0,
                    0
            );

            category.setLayoutParams(
                    categoryParams
            );

            card.addView(category);

            // -----------------------------------------
            // PRICE
            // -----------------------------------------

            TextView price =
                    new TextView(this);

            price.setText(
                    "₹"
                            + service.getPrice()
            );

            price.setTextColor(
                    Color.rgb(
                            15,
                            23,
                            42
                    )
            );

            price.setTextSize(16);

            price.setTypeface(
                    null,
                    Typeface.BOLD
            );

            LinearLayout.LayoutParams
                    priceParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

            priceParams.setMargins(
                    0,
                    8,
                    0,
                    0
            );

            price.setLayoutParams(
                    priceParams
            );

            card.addView(price);

            // -----------------------------------------
            // VIEW SERVICE
            // -----------------------------------------

            TextView viewService =
                    new TextView(this);

            viewService.setText(
                    "View service →"
            );

            viewService.setTextColor(
                    Color.rgb(
                            18,
                            100,
                            229
                    )
            );

            viewService.setTextSize(14);

            viewService.setTypeface(
                    null,
                    Typeface.BOLD
            );

            LinearLayout.LayoutParams
                    viewParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

            viewParams.setMargins(
                    0,
                    14,
                    0,
                    0
            );

            viewService.setLayoutParams(
                    viewParams
            );

            card.addView(viewService);

            // -----------------------------------------
            // CARD CLICK
            // -----------------------------------------

            card.setOnClickListener(v -> {

                Intent intent =
                        new Intent(
                                ServicesActivity.this,
                                ServiceDetailsActivity.class
                        );

                intent.putExtra(
                        "serviceId",
                        service.getId()
                );

                intent.putExtra(
                        "serviceTitle",
                        service.getTitle()
                );

                intent.putExtra(
                        "serviceDescription",
                        service.getDescription()
                );

                intent.putExtra(
                        "serviceCategory",
                        service.getCategory()
                );

                intent.putExtra(
                        "servicePrice",
                        service.getPrice()
                );

                if (service.getProvider() != null) {

                    intent.putExtra(
                            "providerName",
                            service.getProvider()
                                    .getFullName()
                    );

                    intent.putExtra(
                            "college",
                            service.getProvider()
                                    .getCollege()
                    );
                }

                startActivity(intent);
            });

            servicesContainer.addView(
                    card
            );
        }
    }
}