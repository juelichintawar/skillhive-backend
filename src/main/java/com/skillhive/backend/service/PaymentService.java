package com.skillhive.backend.service;

import com.skillhive.backend.model.Payment;
import com.skillhive.backend.model.User;
import com.skillhive.backend.model.Wallet;
import com.skillhive.backend.model.WalletTransaction;
import com.skillhive.backend.repository.PaymentRepository;
import com.skillhive.backend.repository.WalletTransactionRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final WalletService walletService;

    public PaymentService(
            PaymentRepository paymentRepository,
            WalletTransactionRepository walletTransactionRepository,
            WalletService walletService) {

        this.paymentRepository = paymentRepository;
        this.walletTransactionRepository =
                walletTransactionRepository;
        this.walletService = walletService;
    }

    public Payment createPayment(
            User user,
            Double amount) {

        if (amount == null || amount <= 0) {
            throw new RuntimeException(
                    "Amount must be greater than 0"
            );
        }

        if (amount > 10000) {
            throw new RuntimeException(
                    "Maximum top-up amount is ₹10000"
            );
        }

        Payment payment = new Payment();

        payment.setUser(user);
        payment.setAmount(amount);
        payment.setStatus("PENDING");
        payment.setPaymentMethod("MOCK");
        payment.setDescription("Wallet top-up");

        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment confirmPayment(
            User user,
            Long paymentId) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(
                        () -> new RuntimeException(
                                "Payment not found"
                        )
                );

        if (!payment.getUser().getId()
                .equals(user.getId())) {

            throw new RuntimeException(
                    "You cannot confirm this payment"
            );
        }

        if ("SUCCESS".equals(payment.getStatus())) {

            return payment;
        }

        if (!"PENDING".equals(payment.getStatus())) {

            throw new RuntimeException(
                    "Payment cannot be confirmed"
            );
        }

        walletService.addMoney(
                user,
                payment.getAmount()
        );

        Wallet wallet =
                walletService.getWallet(user);

        WalletTransaction transaction =
                new WalletTransaction();

        transaction.setWallet(wallet);
        transaction.setAmount(
                payment.getAmount()
        );
        transaction.setType("TOP_UP");
        transaction.setStatus("SUCCESS");
        transaction.setDescription(
                "Wallet top-up"
        );
        transaction.setReferenceId(
                "PAYMENT_" + payment.getId()
        );

        walletTransactionRepository.save(
                transaction
        );

        payment.setStatus("SUCCESS");
        payment.setGatewayOrderId(
                "MOCK_ORDER_" + payment.getId()
        );
        payment.setGatewayPaymentId(
                "MOCK_PAYMENT_" + payment.getId()
        );

        return paymentRepository.save(payment);
    }
}