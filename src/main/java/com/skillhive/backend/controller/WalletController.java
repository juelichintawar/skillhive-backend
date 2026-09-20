package com.skillhive.backend.controller;

import com.skillhive.backend.model.User;
import com.skillhive.backend.model.Wallet;
import com.skillhive.backend.repository.UserRepository;
import com.skillhive.backend.service.WalletService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private final WalletService walletService;
    private final UserRepository userRepository;

    public WalletController(
            WalletService walletService,
            UserRepository userRepository) {

        this.walletService = walletService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<Wallet> getMyWallet(
            Authentication authentication) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(
                walletService.getWallet(user)
        );
    }

    @PostMapping("/add")
    public ResponseEntity<?> addMoney(
            @RequestParam Double amount,
            Authentication authentication) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(
                walletService.addMoney(user, amount)
        );
    }
}