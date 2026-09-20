package com.skillhive.backend.repository;

import com.skillhive.backend.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByClientId(Long clientId);

    List<Order> findByProviderId(Long providerId);

    List<Order> findByStatus(String status);
}