package com.skillhive.backend.controller;

import com.skillhive.backend.model.User;
import com.skillhive.backend.model.Wallet;
import com.skillhive.backend.model.WalletTransaction;
import com.skillhive.backend.repository.UserRepository;
import com.skillhive.backend.repository.WalletRepository;
import com.skillhive.backend.repository.WalletTransactionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wallet/transactions")
public class WalletTransactionController {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;

    public WalletTransactionController(
            UserRepository userRepository,
            WalletRepository walletRepository,
            WalletTransactionRepository transactionRepository) {

        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    @GetMapping
    public ResponseEntity<?> getMyTransactions(
            Authentication authentication) {

        User user = userRepository.findByEmail(
                authentication.getName()
        ).orElseThrow(
                () -> new RuntimeException("User not found")
        );

        Wallet wallet = walletRepository.findByUserId(
                user.getId()
        ).orElse(null);

        if (wallet == null) {
            return ResponseEntity.ok(List.of());
        }

        List<WalletTransaction> transactions =
                transactionRepository
                        .findByWalletIdOrderByCreatedAtDesc(
                                wallet.getId()
                        );

        return ResponseEntity.ok(transactions);
    }
}