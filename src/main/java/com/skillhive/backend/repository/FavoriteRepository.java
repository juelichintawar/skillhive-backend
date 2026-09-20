package com.skillhive.backend.repository;

import com.skillhive.backend.model.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    Optional<Favorite> findByUserIdAndServiceId(Long userId, Long serviceId);

    List<Favorite> findByUserId(Long userId);

    boolean existsByUserIdAndServiceId(Long userId, Long serviceId);
}