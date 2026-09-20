package com.skillhive.backend.controller;

import com.skillhive.backend.model.Favorite;
import com.skillhive.backend.model.Service;
import com.skillhive.backend.model.User;
import com.skillhive.backend.repository.FavoriteRepository;
import com.skillhive.backend.repository.ServiceRepository;
import com.skillhive.backend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final FavoriteRepository favoriteRepository;
    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;

    public FavoriteController(
            FavoriteRepository favoriteRepository,
            ServiceRepository serviceRepository,
            UserRepository userRepository) {

        this.favoriteRepository = favoriteRepository;
        this.serviceRepository = serviceRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/service/{serviceId}")
    public ResponseEntity<?> addFavorite(
            @PathVariable Long serviceId,
            Authentication authentication) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));

        if (favoriteRepository.existsByUserIdAndServiceId(
                user.getId(), serviceId)) {

            return ResponseEntity.badRequest()
                    .body("Service is already in favorites");
        }

        Favorite favorite = new Favorite();
        favorite.setUser(user);
        favorite.setService(service);

        return ResponseEntity.ok(
                favoriteRepository.save(favorite)
        );
    }

    @GetMapping("/my")
    public ResponseEntity<List<Favorite>> getMyFavorites(
            Authentication authentication) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(
                favoriteRepository.findByUserId(user.getId())
        );
    }

    @DeleteMapping("/service/{serviceId}")
    public ResponseEntity<?> removeFavorite(
            @PathVariable Long serviceId,
            Authentication authentication) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Favorite favorite = favoriteRepository
                .findByUserIdAndServiceId(user.getId(), serviceId)
                .orElse(null);

        if (favorite == null) {
            return ResponseEntity.notFound().build();
        }

        favoriteRepository.delete(favorite);

        return ResponseEntity.ok("Service removed from favorites");
    }
}