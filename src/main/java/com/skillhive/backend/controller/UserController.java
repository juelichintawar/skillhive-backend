package com.skillhive.backend.controller;

import com.skillhive.backend.dto.UpdateProfileRequest;
import com.skillhive.backend.model.User;
import com.skillhive.backend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication authentication) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        return ResponseEntity.ok(new ProfileResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getCollege(),
                user.getBio(),
                user.getProfileImageUrl(),
                user.getRole(),
                user.getCreatedAt()
        ));
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
            Authentication authentication,
            @RequestBody UpdateProfileRequest request) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setCollege(request.getCollege());
        user.setBio(request.getBio());
        user.setProfileImageUrl(request.getProfileImageUrl());

        User updatedUser = userRepository.save(user);

        return ResponseEntity.ok(new ProfileResponse(
                updatedUser.getId(),
                updatedUser.getFullName(),
                updatedUser.getEmail(),
                updatedUser.getPhone(),
                updatedUser.getCollege(),
                updatedUser.getBio(),
                updatedUser.getProfileImageUrl(),
                updatedUser.getRole(),
                updatedUser.getCreatedAt()
        ));
    }

    public record ProfileResponse(
            Long id,
            String fullName,
            String email,
            String phone,
            String college,
            String bio,
            String profileImageUrl,
            String role,
            java.time.LocalDateTime createdAt
    ) {
    }
}