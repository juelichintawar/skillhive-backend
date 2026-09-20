package com.skillhive.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "service_id", nullable = false)
    private Service service;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @ManyToOne
    @JoinColumn(name = "provider_id", nullable = false)
    private User provider;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private String status = "REQUESTED";

    /*
     * Payment hold tracking
     *
     * When the client creates an order, the money will be
     * moved from the client's available wallet balance into
     * the order's held amount.
     *
     * The provider will receive this amount only after the
     * client accepts the completed work.
     */
    @Column(nullable = false)
    private Double heldAmount = 0.0;

    @Column(nullable = false)
    private Boolean paymentReleased = false;

    @Column(nullable = false)
    private Boolean paymentRefunded = false;

    @Column(length = 150)
    private String projectTitle;

    @Column(length = 3000)
    private String requirements;

    private String deadline;

    @Column(length = 3000)
    private String projectMessage;

    @Column(length = 1000)
    private String projectLink;

    @Column(length = 3000)
    private String revisionMessage;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public Order() {
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public User getClient() {
        return client;
    }

    public void setClient(User client) {
        this.client = client;
    }

    public User getProvider() {
        return provider;
    }

    public void setProvider(User provider) {
        this.provider = provider;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getHeldAmount() {
        return heldAmount;
    }

    public void setHeldAmount(Double heldAmount) {
        this.heldAmount = heldAmount;
    }

    public Boolean getPaymentReleased() {
        return paymentReleased;
    }

    public void setPaymentReleased(Boolean paymentReleased) {
        this.paymentReleased = paymentReleased;
    }

    public Boolean getPaymentRefunded() {
        return paymentRefunded;
    }

    public void setPaymentRefunded(Boolean paymentRefunded) {
        this.paymentRefunded = paymentRefunded;
    }

    public String getProjectTitle() {
        return projectTitle;
    }

    public void setProjectTitle(String projectTitle) {
        this.projectTitle = projectTitle;
    }

    public String getRequirements() {
        return requirements;
    }

    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public String getProjectMessage() {
        return projectMessage;
    }

    public void setProjectMessage(String projectMessage) {
        this.projectMessage = projectMessage;
    }

    public String getProjectLink() {
        return projectLink;
    }

    public void setProjectLink(String projectLink) {
        this.projectLink = projectLink;
    }

    public String getRevisionMessage() {
        return revisionMessage;
    }

    public void setRevisionMessage(String revisionMessage) {
        this.revisionMessage = revisionMessage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}