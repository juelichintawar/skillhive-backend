package com.example.skillhive;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.skillhive.model.Review;
import com.example.skillhive.network.RetrofitClient;
import com.example.skillhive.network.ReviewApi;
import com.example.skillhive.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReviewActivity extends AppCompatActivity {

    private RatingBar ratingBarReview;
    private TextView tvRatingValue;
    private EditText etReviewComment;
    private ProgressBar progressBarReview;
    private Button btnSubmitReview;

    private ReviewApi reviewApi;
    private SessionManager sessionManager;

    private Long orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_review);

        ratingBarReview = findViewById(R.id.ratingBarReview);
        tvRatingValue = findViewById(R.id.tvRatingValue);
        etReviewComment = findViewById(R.id.etReviewComment);
        progressBarReview = findViewById(R.id.progressBarReview);
        btnSubmitReview = findViewById(R.id.btnSubmitReview);

        sessionManager = new SessionManager(this);

        reviewApi = RetrofitClient
                .getInstance()
                .create(ReviewApi.class);

        orderId = getIntent().getLongExtra("orderId", -1);

        if (orderId == -1) {
            Toast.makeText(
                    this,
                    "Invalid order",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        ratingBarReview.setOnRatingBarChangeListener(
                (ratingBar, rating, fromUser) -> {

                    tvRatingValue.setText(
                            ((int) rating) + " / 5"
                    );
                }
        );

        btnSubmitReview.setOnClickListener(v -> submitReview());
    }

    private void submitReview() {

        if (orderId == -1) {
            Toast.makeText(
                    this,
                    "Invalid order",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        int rating = (int) ratingBarReview.getRating();

        if (rating < 1 || rating > 5) {
            Toast.makeText(
                    this,
                    "Please select a rating",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        String comment =
                etReviewComment.getText()
                        .toString()
                        .trim();

        Review review = new Review();

        review.setRating(rating);
        review.setComment(comment);

        String token =
                sessionManager.getToken();

        if (token == null || token.isEmpty()) {

            Toast.makeText(
                    this,
                    "Please login again",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        setLoading(true);

        reviewApi.createReview(
                orderId,
                review,
                "Bearer " + token
        ).enqueue(new Callback<Review>() {

            @Override
            public void onResponse(
                    Call<Review> call,
                    Response<Review> response) {

                setLoading(false);

                if (response.isSuccessful()
                        && response.body() != null) {

                    Toast.makeText(
                            ReviewActivity.this,
                            "Review submitted successfully",
                            Toast.LENGTH_SHORT
                    ).show();

                    finish();

                } else {

                    String message =
                            "Failed to submit review";

                    if (response.code() == 400) {
                        message =
                                "This order cannot be reviewed";
                    } else if (response.code() == 403) {
                        message =
                                "You cannot review this order";
                    }

                    Toast.makeText(
                            ReviewActivity.this,
                            message,
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onFailure(
                    Call<Review> call,
                    Throwable t) {

                setLoading(false);

                Toast.makeText(
                        ReviewActivity.this,
                        "Connection failed: "
                                + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void setLoading(boolean loading) {

        if (loading) {

            progressBarReview.setVisibility(View.VISIBLE);

            btnSubmitReview.setEnabled(false);

            btnSubmitReview.setText(
                    "Submitting Review..."
            );

        } else {

            progressBarReview.setVisibility(View.GONE);

            btnSubmitReview.setEnabled(true);

            btnSubmitReview.setText(
                    "Submit Review"
            );
        }
    }
}