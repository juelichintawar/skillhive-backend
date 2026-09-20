package com.skillhive.backend.service;

import com.skillhive.backend.model.User;
import com.skillhive.backend.model.Wallet;
import com.skillhive.backend.model.WalletTransaction;
import com.skillhive.backend.repository.WalletRepository;
import com.skillhive.backend.repository.WalletTransactionRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WalletService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;

    public WalletService(
            WalletRepository walletRepository,
            WalletTransactionRepository walletTransactionRepository) {

        this.walletRepository = walletRepository;
        this.walletTransactionRepository = walletTransactionRepository;
    }

    // =====================================================
    // CREATE WALLET
    // =====================================================

    public Wallet createWallet(User user) {

        if (walletRepository.existsByUserId(user.getId())) {

            return walletRepository.findByUserId(user.getId())
                    .orElseThrow(
                            () -> new RuntimeException(
                                    "Wallet not found"
                            )
                    );
        }

        Wallet wallet = new Wallet();

        wallet.setUser(user);
        wallet.setBalance(0.0);

        return walletRepository.save(wallet);
    }


    // =====================================================
    // GET WALLET
    // =====================================================

    public Wallet getWallet(User user) {

        return walletRepository.findByUserId(user.getId())
                .orElseGet(
                        () -> createWallet(user)
                );
    }


    // =====================================================
    // ADD MONEY
    // =====================================================

    public Wallet addMoney(
            User user,
            Double amount) {

        if (amount == null || amount <= 0) {

            throw new RuntimeException(
                    "Amount must be greater than 0"
            );
        }

        Wallet wallet = getWallet(user);

        wallet.setBalance(
                wallet.getBalance() + amount
        );

        return walletRepository.save(wallet);
    }


    // =====================================================
    // NORMAL WALLET TRANSFER
    // =====================================================

    @Transactional
    public void transferMoney(
            User sender,
            User receiver,
            Double amount) {

        validateAmount(amount);

        Wallet senderWallet =
                getWallet(sender);

        Wallet receiverWallet =
                getWallet(receiver);

        if (senderWallet.getBalance() < amount) {

            throw new RuntimeException(
                    "Insufficient wallet balance"
            );
        }

        senderWallet.setBalance(
                senderWallet.getBalance() - amount
        );

        receiverWallet.setBalance(
                receiverWallet.getBalance() + amount
        );

        walletRepository.save(senderWallet);
        walletRepository.save(receiverWallet);
    }


    // =====================================================
    // NORMAL REFUND
    // =====================================================

    @Transactional
    public void refundMoney(
            User sender,
            User receiver,
            Double amount) {

        validateAmount(amount);

        Wallet senderWallet =
                getWallet(sender);

        Wallet receiverWallet =
                getWallet(receiver);

        if (receiverWallet.getBalance() < amount) {

            throw new RuntimeException(
                    "Provider does not have enough balance for refund"
            );
        }

        receiverWallet.setBalance(
                receiverWallet.getBalance() - amount
        );

        senderWallet.setBalance(
                senderWallet.getBalance() + amount
        );

        walletRepository.save(receiverWallet);
        walletRepository.save(senderWallet);
    }


    // =====================================================
    // HOLD PAYMENT
    // =====================================================

    @Transactional
    public void holdMoney(
            User client,
            Double amount) {

        holdMoney(
                client,
                amount,
                null
        );
    }


    /*
     * Holds money from the client's available balance.
     *
     * The provider does NOT receive the money.
     *
     * A PAYMENT_HELD transaction is created.
     */
    @Transactional
    public void holdMoney(
            User client,
            Double amount,
            String referenceId) {

        validateAmount(amount);

        Wallet clientWallet =
                getWallet(client);

        if (clientWallet.getBalance() < amount) {

            throw new RuntimeException(
                    "Insufficient wallet balance"
            );
        }

        clientWallet.setBalance(
                clientWallet.getBalance() - amount
        );

        walletRepository.save(clientWallet);

        // ---------------------------------------------
        // TRANSACTION HISTORY
        // ---------------------------------------------

        WalletTransaction transaction =
                new WalletTransaction();

        transaction.setWallet(clientWallet);

        /*
         * Negative amount because money left
         * the client's available wallet.
         */
        transaction.setAmount(-amount);

        transaction.setType(
                "PAYMENT_HELD"
        );

        transaction.setStatus(
                "SUCCESS"
        );

        transaction.setDescription(
                "Payment held for SkillHive order"
        );

        transaction.setReferenceId(
                referenceId
        );

        walletTransactionRepository.save(
                transaction
        );
    }


    // =====================================================
    // RELEASE PAYMENT
    // =====================================================

    @Transactional
    public void releaseMoney(
            User provider,
            Double amount) {

        releaseMoney(
                provider,
                amount,
                null
        );
    }


    /*
     * Releases held payment to provider.
     *
     * A PAYMENT_RELEASED transaction is created.
     */
    @Transactional
    public void releaseMoney(
            User provider,
            Double amount,
            String referenceId) {

        validateAmount(amount);

        Wallet providerWallet =
                getWallet(provider);

        providerWallet.setBalance(
                providerWallet.getBalance() + amount
        );

        walletRepository.save(providerWallet);

        // ---------------------------------------------
        // TRANSACTION HISTORY
        // ---------------------------------------------

        WalletTransaction transaction =
                new WalletTransaction();

        transaction.setWallet(providerWallet);

        transaction.setAmount(
                amount
        );

        transaction.setType(
                "PAYMENT_RELEASED"
        );

        transaction.setStatus(
                "SUCCESS"
        );

        transaction.setDescription(
                "Payment received for SkillHive order"
        );

        transaction.setReferenceId(
                referenceId
        );

        walletTransactionRepository.save(
                transaction
        );
    }


    // =====================================================
    // REFUND HELD PAYMENT
    // =====================================================

    @Transactional
    public void refundHeldMoney(
            User client,
            Double amount) {

        refundHeldMoney(
                client,
                amount,
                null
        );
    }


    /*
     * Returns previously held money to the client.
     *
     * A PAYMENT_REFUNDED transaction is created.
     */
    @Transactional
    public void refundHeldMoney(
            User client,
            Double amount,
            String referenceId) {

        validateAmount(amount);

        Wallet clientWallet =
                getWallet(client);

        clientWallet.setBalance(
                clientWallet.getBalance() + amount
        );

        walletRepository.save(clientWallet);

        // ---------------------------------------------
        // TRANSACTION HISTORY
        // ---------------------------------------------

        WalletTransaction transaction =
                new WalletTransaction();

        transaction.setWallet(clientWallet);

        transaction.setAmount(
                amount
        );

        transaction.setType(
                "PAYMENT_REFUNDED"
        );

        transaction.setStatus(
                "SUCCESS"
        );

        transaction.setDescription(
                "Payment refunded for SkillHive order"
        );

        transaction.setReferenceId(
                referenceId
        );

        walletTransactionRepository.save(
                transaction
        );
    }


    // =====================================================
    // VALIDATE AMOUNT
    // =====================================================

    private void validateAmount(
            Double amount) {

        if (amount == null || amount <= 0) {

            throw new RuntimeException(
                    "Amount must be greater than 0"
            );
        }
    }
}