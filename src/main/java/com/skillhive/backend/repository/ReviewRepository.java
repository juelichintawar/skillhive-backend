package com.skillhive.backend.repository;

import com.skillhive.backend.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Optional<Review> findByOrderId(Long orderId);

    List<Review> findByProviderId(Long providerId);

    boolean existsByOrderId(Long orderId);

    long countByProviderId(Long providerId);

    Double findAverageRatingByProviderId(Long providerId);
}