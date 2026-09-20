package com.skillhive.backend.controller;

import com.skillhive.backend.model.Payment;
import com.skillhive.backend.model.User;
import com.skillhive.backend.repository.UserRepository;
import com.skillhive.backend.service.PaymentService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final UserRepository userRepository;

    public PaymentController(
            PaymentService paymentService,
            UserRepository userRepository) {

        this.paymentService = paymentService;
        this.userRepository = userRepository;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createPayment(
            @RequestParam Double amount,
            Authentication authentication) {

        User user = userRepository.findByEmail(
                authentication.getName()
        ).orElseThrow(
                () -> new RuntimeException("User not found")
        );

        try {

            Payment payment =
                    paymentService.createPayment(
                            user,
                            amount
                    );

            return ResponseEntity.ok(payment);

        } catch (RuntimeException e) {

            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }

    @PostMapping("/{paymentId}/confirm")
    public ResponseEntity<?> confirmPayment(
            @PathVariable Long paymentId,
            Authentication authentication) {

        User user = userRepository.findByEmail(
                authentication.getName()
        ).orElseThrow(
                () -> new RuntimeException("User not found")
        );

        try {

            Payment payment =
                    paymentService.confirmPayment(
                            user,
                            paymentId
                    );

            return ResponseEntity.ok(payment);

        } catch (RuntimeException e) {

            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }
}