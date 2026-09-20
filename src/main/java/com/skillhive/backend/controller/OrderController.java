package com.skillhive.backend.controller;

import com.skillhive.backend.model.Order;
import com.skillhive.backend.model.Service;
import com.skillhive.backend.model.User;
import com.skillhive.backend.repository.OrderRepository;
import com.skillhive.backend.repository.ServiceRepository;
import com.skillhive.backend.repository.UserRepository;
import com.skillhive.backend.service.NotificationService;
import com.skillhive.backend.service.WalletService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderRepository orderRepository;
    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final WalletService walletService;

    public OrderController(
            OrderRepository orderRepository,
            ServiceRepository serviceRepository,
            UserRepository userRepository,
            NotificationService notificationService,
            WalletService walletService) {

        this.orderRepository = orderRepository;
        this.serviceRepository = serviceRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.walletService = walletService;
    }

    // =====================================================
    // CREATE ORDER
    // =====================================================

    @PostMapping("/service/{serviceId}")
    @Transactional
    public ResponseEntity<?> createOrder(
            @PathVariable Long serviceId,
            @RequestParam(required = false) String projectTitle,
            @RequestParam(required = false) String requirements,
            @RequestParam(required = false) String deadline,
            Authentication authentication) {

        User client = userRepository.findByEmail(
                authentication.getName()
        ).orElseThrow(
                () -> new RuntimeException("User not found")
        );

        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(
                        () -> new RuntimeException("Service not found")
                );

        if (service.getProvider().getId().equals(client.getId())) {
            return ResponseEntity.badRequest()
                    .body("You cannot order your own service");
        }

        if (!"ACTIVE".equals(service.getStatus())) {
            return ResponseEntity.badRequest()
                    .body("Service is not active");
        }

        if (projectTitle == null
                || projectTitle.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("Project title is required");
        }

        if (requirements == null
                || requirements.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("Project requirements are required");
        }

        if (deadline == null
                || deadline.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("Project deadline is required");
        }

        // -------------------------------------------------
        // CREATE ORDER FIRST
        //
        // We need the generated order ID so that the wallet
        // transaction can store ORDER_<id> as reference.
        // -------------------------------------------------

        Order order = new Order();

        order.setService(service);
        order.setClient(client);
        order.setProvider(service.getProvider());
        order.setPrice(service.getPrice());

        order.setProjectTitle(
                projectTitle.trim()
        );

        order.setRequirements(
                requirements.trim()
        );

        order.setDeadline(
                deadline.trim()
        );

        order.setStatus("REQUESTED");

        order.setHeldAmount(0.0);
        order.setPaymentReleased(false);
        order.setPaymentRefunded(false);

        Order savedOrder = orderRepository.save(order);

        // -------------------------------------------------
        // HOLD PAYMENT
        // -------------------------------------------------

        try {

            walletService.holdMoney(
                    client,
                    service.getPrice(),
                    "ORDER_" + savedOrder.getId()
            );

        } catch (RuntimeException e) {

            /*
             * Since this method is @Transactional, throwing the
             * exception would roll back the newly-created order.
             *
             * We return a clean error response here.
             */
            throw e;
        }

        // -------------------------------------------------
        // UPDATE PAYMENT HOLD
        // -------------------------------------------------

        savedOrder.setHeldAmount(
                service.getPrice()
        );

        savedOrder.setPaymentReleased(false);
        savedOrder.setPaymentRefunded(false);

        savedOrder = orderRepository.save(savedOrder);

        // -------------------------------------------------
        // NOTIFY PROVIDER
        // -------------------------------------------------

        notificationService.createNotification(
                service.getProvider(),
                "New order received for your service: "
                        + service.getTitle(),
                "ORDER_CREATED"
        );

        return ResponseEntity.ok(savedOrder);
    }


    // =====================================================
    // MY ORDERS
    // =====================================================

    @GetMapping("/my")
    public ResponseEntity<List<Order>> getMyOrders(
            Authentication authentication) {

        User client = userRepository.findByEmail(
                authentication.getName()
        ).orElseThrow(
                () -> new RuntimeException("User not found")
        );

        return ResponseEntity.ok(
                orderRepository.findByClientId(
                        client.getId()
                )
        );
    }


    // =====================================================
    // RECEIVED ORDERS
    // =====================================================

    @GetMapping("/received")
    public ResponseEntity<List<Order>> getReceivedOrders(
            Authentication authentication) {

        User provider = userRepository.findByEmail(
                authentication.getName()
        ).orElseThrow(
                () -> new RuntimeException("User not found")
        );

        return ResponseEntity.ok(
                orderRepository.findByProviderId(
                        provider.getId()
                )
        );
    }


    // =====================================================
    // UPDATE ORDER STATUS
    // =====================================================

    @PutMapping("/{id}/status")
    @Transactional
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam String status,

            @RequestParam(required = false)
            String projectMessage,

            @RequestParam(required = false)
            String projectLink,

            Authentication authentication) {

        User provider = userRepository.findByEmail(
                authentication.getName()
        ).orElseThrow(
                () -> new RuntimeException("User not found")
        );

        Order order = orderRepository.findById(id)
                .orElseThrow(
                        () -> new RuntimeException("Order not found")
                );

        if (!order.getProvider().getId()
                .equals(provider.getId())) {

            return ResponseEntity.status(403)
                    .body("You cannot update this order");
        }

        // -------------------------------------------------
        // REQUESTED
        // -------------------------------------------------

        if ("REQUESTED".equals(order.getStatus())) {

            if (!"ACCEPTED".equals(status)
                    && !"REJECTED".equals(status)) {

                return ResponseEntity.badRequest()
                        .body(
                                "Requested order can only be "
                                        + "ACCEPTED or REJECTED"
                        );
            }
        }

        // -------------------------------------------------
        // ACCEPTED
        // -------------------------------------------------

        else if ("ACCEPTED".equals(order.getStatus())) {

            if (!"IN_PROGRESS".equals(status)) {

                return ResponseEntity.badRequest()
                        .body(
                                "Accepted order can only "
                                        + "move to IN_PROGRESS"
                        );
            }
        }

        // -------------------------------------------------
        // REVISION REQUESTED
        // -------------------------------------------------

        else if ("REVISION_REQUESTED".equals(order.getStatus())) {

            if (!"IN_PROGRESS".equals(status)) {

                return ResponseEntity.badRequest()
                        .body(
                                "Revision requested order can only "
                                        + "move to IN_PROGRESS"
                        );
            }

            // Previous project submission remains available.
        }

        // -------------------------------------------------
        // IN PROGRESS
        // -------------------------------------------------

        else if ("IN_PROGRESS".equals(order.getStatus())) {

            if (!"SUBMITTED".equals(status)) {

                return ResponseEntity.badRequest()
                        .body(
                                "In-progress order can only "
                                        + "move to SUBMITTED"
                        );
            }

            if (projectMessage == null
                    || projectMessage.trim().isEmpty()) {

                return ResponseEntity.badRequest()
                        .body(
                                "Project submission message "
                                        + "is required"
                        );
            }

            if (projectLink == null
                    || projectLink.trim().isEmpty()) {

                return ResponseEntity.badRequest()
                        .body(
                                "Project link is required"
                        );
            }

            order.setProjectMessage(
                    projectMessage.trim()
            );

            order.setProjectLink(
                    projectLink.trim()
            );
        }

        // -------------------------------------------------
        // SUBMITTED
        // -------------------------------------------------

        else if ("SUBMITTED".equals(order.getStatus())) {

            return ResponseEntity.badRequest()
                    .body(
                            "Submitted order must be completed "
                                    + "by the client or sent back "
                                    + "for revision"
                    );
        }

        // -------------------------------------------------
        // OTHER STATES
        // -------------------------------------------------

        else {

            return ResponseEntity.badRequest()
                    .body(
                            "Invalid order status transition"
                    );
        }

        // =================================================
        // REJECT ORDER
        // =================================================

        if ("REJECTED".equals(status)) {

            try {

                // -----------------------------------------
                // NEW HELD-PAYMENT ORDERS
                // -----------------------------------------

                if (order.getHeldAmount() != null
                        && order.getHeldAmount() > 0
                        && !Boolean.TRUE.equals(
                        order.getPaymentReleased())
                        && !Boolean.TRUE.equals(
                        order.getPaymentRefunded())) {

                    walletService.refundHeldMoney(
                            order.getClient(),
                            order.getHeldAmount(),
                            "ORDER_" + order.getId()
                    );

                    order.setPaymentRefunded(true);
                    order.setHeldAmount(0.0);
                }

                // -----------------------------------------
                // LEGACY ORDERS
                // -----------------------------------------

                else if (order.getHeldAmount() == null
                        || order.getHeldAmount() <= 0) {

                    if (!Boolean.TRUE.equals(
                            order.getPaymentReleased())
                            && !Boolean.TRUE.equals(
                            order.getPaymentRefunded())) {

                        walletService.refundMoney(
                                order.getClient(),
                                order.getProvider(),
                                order.getPrice()
                        );

                        order.setPaymentRefunded(true);
                    }
                }

            } catch (RuntimeException e) {

                return ResponseEntity.badRequest()
                        .body(e.getMessage());
            }
        }

        // -------------------------------------------------
        // SAVE STATUS
        // -------------------------------------------------

        order.setStatus(status);

        Order savedOrder =
                orderRepository.save(order);

        // -------------------------------------------------
        // NOTIFY CLIENT WHEN SUBMITTED
        // -------------------------------------------------

        if ("SUBMITTED".equals(status)) {

            notificationService.createNotification(
                    order.getClient(),
                    "Your project has been submitted by "
                            + order.getProvider().getFullName(),
                    "PROJECT_SUBMITTED"
            );
        }

        // -------------------------------------------------
        // NOTIFY PROVIDER WHEN REJECTED
        // -------------------------------------------------

        if ("REJECTED".equals(status)) {

            notificationService.createNotification(
                    order.getProvider(),
                    "Your order was rejected.",
                    "ORDER_REJECTED"
            );
        }

        return ResponseEntity.ok(savedOrder);
    }


    // =====================================================
    // REQUEST REVISION
    // =====================================================

    @PutMapping("/{id}/revision")
    @Transactional
    public ResponseEntity<?> requestRevision(
            @PathVariable Long id,
            @RequestParam String revisionMessage,
            Authentication authentication) {

        User client = userRepository.findByEmail(
                authentication.getName()
        ).orElseThrow(
                () -> new RuntimeException("User not found")
        );

        Order order = orderRepository.findById(id)
                .orElseThrow(
                        () -> new RuntimeException("Order not found")
                );

        if (!order.getClient().getId()
                .equals(client.getId())) {

            return ResponseEntity.status(403)
                    .body(
                            "You cannot request changes "
                                    + "for this order"
                    );
        }

        if (!"SUBMITTED".equals(order.getStatus())) {

            return ResponseEntity.badRequest()
                    .body(
                            "Only submitted projects can "
                                    + "be sent for revision"
                    );
        }

        if (revisionMessage == null
                || revisionMessage.trim().isEmpty()) {

            return ResponseEntity.badRequest()
                    .body(
                            "Please describe the changes "
                                    + "you want"
                    );
        }

        order.setRevisionMessage(
                revisionMessage.trim()
        );

        order.setStatus(
                "REVISION_REQUESTED"
        );

        Order savedOrder =
                orderRepository.save(order);

        notificationService.createNotification(
                order.getProvider(),
                "The client requested changes to your submitted project: "
                        + revisionMessage.trim(),
                "REVISION_REQUESTED"
        );

        return ResponseEntity.ok(savedOrder);
    }


    // =====================================================
    // COMPLETE ORDER
    // =====================================================

    @PutMapping("/{id}/complete")
    @Transactional
    public ResponseEntity<?> completeOrder(
            @PathVariable Long id,
            Authentication authentication) {

        User client = userRepository.findByEmail(
                authentication.getName()
        ).orElseThrow(
                () -> new RuntimeException("User not found")
        );

        Order order = orderRepository.findById(id)
                .orElseThrow(
                        () -> new RuntimeException("Order not found")
                );

        if (!order.getClient().getId()
                .equals(client.getId())) {

            return ResponseEntity.status(403)
                    .body(
                            "You cannot complete this order"
                    );
        }

        if (!"SUBMITTED".equals(order.getStatus())) {

            return ResponseEntity.badRequest()
                    .body(
                            "Only submitted orders can be completed"
                    );
        }

        // =================================================
        // RELEASE HELD PAYMENT
        // =================================================

        try {

            if (order.getHeldAmount() != null
                    && order.getHeldAmount() > 0
                    && !Boolean.TRUE.equals(
                    order.getPaymentReleased())
                    && !Boolean.TRUE.equals(
                    order.getPaymentRefunded())) {

                walletService.releaseMoney(
                        order.getProvider(),
                        order.getHeldAmount(),
                        "ORDER_" + order.getId()
                );

                order.setPaymentReleased(true);
                order.setHeldAmount(0.0);
            }

            // Legacy orders already paid the provider.
            // No second payment is made.

        } catch (RuntimeException e) {

            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }

        // -------------------------------------------------
        // COMPLETE ORDER
        // -------------------------------------------------

        order.setStatus("COMPLETED");

        Order savedOrder =
                orderRepository.save(order);

        // -------------------------------------------------
        // NOTIFY PROVIDER
        // -------------------------------------------------

        notificationService.createNotification(
                order.getProvider(),
                "Client accepted and completed your project.",
                "ORDER_COMPLETED"
        );

        return ResponseEntity.ok(savedOrder);
    }


    // =====================================================
    // GET ORDER BY ID
    // =====================================================

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(
            @PathVariable Long id) {

        return orderRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(
                        () -> ResponseEntity.notFound().build()
                );
    }
}