package com.skillhive.backend.repository;

import com.skillhive.backend.model.Service;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceRepository extends JpaRepository<Service, Long> {

    List<Service> findByProviderId(Long providerId);

    List<Service> findByCategory(String category);

    List<Service> findByStatus(String status);

    List<Service> findByTitleContainingIgnoreCaseAndStatus(
            String title,
            String status
    );

    List<Service> findByDescriptionContainingIgnoreCaseAndStatus(
            String description,
            String status
    );

    List<Service> findByPriceBetweenAndStatus(
            Double minPrice,
            Double maxPrice,
            String status
    );
}