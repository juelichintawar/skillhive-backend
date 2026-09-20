package com.skillhive.backend.controller;

import com.skillhive.backend.model.Order;
import com.skillhive.backend.model.Review;
import com.skillhive.backend.model.User;
import com.skillhive.backend.repository.OrderRepository;
import com.skillhive.backend.repository.ReviewRepository;
import com.skillhive.backend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public ReviewController(
            ReviewRepository reviewRepository,
            OrderRepository orderRepository,
            UserRepository userRepository) {

        this.reviewRepository = reviewRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/order/{orderId}")
    public ResponseEntity<?> createReview(
            @PathVariable Long orderId,
            @RequestBody Review review,
            Authentication authentication) {

        User client = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getClient().getId().equals(client.getId())) {
            return ResponseEntity.status(403)
                    .body("You cannot review this order");
        }

        if (!"COMPLETED".equals(order.getStatus())) {
            return ResponseEntity.badRequest()
                    .body("Only completed orders can be reviewed");
        }

        if (reviewRepository.existsByOrderId(orderId)) {
            return ResponseEntity.badRequest()
                    .body("This order has already been reviewed");
        }

        if (review.getRating() == null
                || review.getRating() < 1
                || review.getRating() > 5) {

            return ResponseEntity.badRequest()
                    .body("Rating must be between 1 and 5");
        }

        review.setOrder(order);
        review.setClient(client);
        review.setProvider(order.getProvider());

        return ResponseEntity.ok(
                reviewRepository.save(review)
        );
    }

    @GetMapping("/provider/{providerId}")
    public ResponseEntity<List<Review>> getProviderReviews(
            @PathVariable Long providerId) {

        return ResponseEntity.ok(
                reviewRepository.findByProviderId(providerId)
        );
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<?> getOrderReview(
            @PathVariable Long orderId) {

        return reviewRepository.findByOrderId(orderId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/rating-summary/{providerId}")
    public ResponseEntity<?> getProviderRatingSummary(
            @PathVariable Long providerId) {

        long totalReviews =
                reviewRepository.countByProviderId(providerId);

        Double averageRating =
                reviewRepository.findAverageRatingByProviderId(providerId);

        if (averageRating == null) {
            averageRating = 0.0;
        }

        Map<String, Object> summary =
                new HashMap<>();

        summary.put("providerId", providerId);
        summary.put(
                "averageRating",
                Math.round(averageRating * 10.0) / 10.0
        );
        summary.put("totalReviews", totalReviews);

        return ResponseEntity.ok(summary);
    }
}