package com.skillhive.backend.controller;

import com.skillhive.backend.model.Service;
import com.skillhive.backend.model.User;
import com.skillhive.backend.repository.ServiceRepository;
import com.skillhive.backend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/services")
public class ServiceController {

    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;

    public ServiceController(
            ServiceRepository serviceRepository,
            UserRepository userRepository) {

        this.serviceRepository = serviceRepository;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<?> createService(
            @RequestBody Service service,
            Authentication authentication) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        service.setProvider(user);
        service.setStatus("ACTIVE");

        return ResponseEntity.ok(
                serviceRepository.save(service)
        );
    }

    @GetMapping
    public ResponseEntity<List<Service>> getAllServices() {

        return ResponseEntity.ok(
                serviceRepository.findByStatus("ACTIVE")
        );
    }

    @GetMapping("/my")
    public ResponseEntity<List<Service>> getMyServices(
            Authentication authentication) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(
                serviceRepository.findByProviderId(user.getId())
        );
    }

    @GetMapping("/search")
    public ResponseEntity<List<Service>> searchServices(
            @RequestParam String keyword) {

        List<Service> titleResults =
                serviceRepository
                        .findByTitleContainingIgnoreCaseAndStatus(
                                keyword,
                                "ACTIVE"
                        );

        List<Service> descriptionResults =
                serviceRepository
                        .findByDescriptionContainingIgnoreCaseAndStatus(
                                keyword,
                                "ACTIVE"
                        );

        List<Service> results = new ArrayList<>(titleResults);

        for (Service service : descriptionResults) {
            if (!results.contains(service)) {
                results.add(service);
            }
        }

        return ResponseEntity.ok(results);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<Service>> getServicesByCategory(
            @PathVariable String category) {

        return ResponseEntity.ok(
                serviceRepository.findByCategory(category)
        );
    }

    @GetMapping("/price")
    public ResponseEntity<List<Service>> getServicesByPrice(
            @RequestParam Double min,
            @RequestParam Double max) {

        return ResponseEntity.ok(
                serviceRepository.findByPriceBetweenAndStatus(
                        min,
                        max,
                        "ACTIVE"
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getServiceById(
            @PathVariable Long id) {

        return serviceRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateService(
            @PathVariable Long id,
            @RequestBody Service updatedService,
            Authentication authentication) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service not found"));

        if (!service.getProvider().getEmail().equals(user.getEmail())) {
            return ResponseEntity.status(403)
                    .body("You cannot update this service");
        }

        service.setTitle(updatedService.getTitle());
        service.setDescription(updatedService.getDescription());
        service.setPrice(updatedService.getPrice());
        service.setCategory(updatedService.getCategory());
        service.setImageUrl(updatedService.getImageUrl());

        return ResponseEntity.ok(
                serviceRepository.save(service)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteService(
            @PathVariable Long id,
            Authentication authentication) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service not found"));

        if (!service.getProvider().getId().equals(user.getId())) {
            return ResponseEntity.status(403)
                    .body("You cannot delete this service");
        }

        service.setStatus("INACTIVE");
        serviceRepository.save(service);

        return ResponseEntity.ok("Service deleted successfully");
    }

}