package com.example.paymentservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity class representing a payment transaction.
 */
@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    
    /**
     * Possible payment statuses.
     */
    public enum PaymentStatus {
        PENDING,
        COMPLETED,
        FAILED,
        REFUNDED
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "order_id", nullable = false, unique = true)
    private String orderId;
    
    @Column(name = "amount", nullable = false)
    private BigDecimal amount;
    
    @Column(name = "payment_method", nullable = false)
    private String paymentMethod;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;
    
    @Column(name = "transaction_id")
    private String transactionId;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * Pre-persist hook to set creation and update timestamps.
     */
    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Pre-update hook to set update timestamp.
     */
    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public boolean isCompleted() {
        return status == PaymentStatus.COMPLETED;
    }

    public boolean isFailed() {
        return status == PaymentStatus.FAILED;
    }

    public boolean isPending() {
        return status == PaymentStatus.PENDING;
    }

    public boolean isRefunded() {
        return status == PaymentStatus.REFUNDED;
    }

    public void markAsCompleted(String transactionId) {
        if (!isPending()) {
            throw new com.example.paymentservice.exception.InvalidPaymentStateException(
                    orderId, status, "complete");
        }
        this.status = PaymentStatus.COMPLETED;
        this.transactionId = transactionId;
    }

    public void markAsFailed() {
        if (!isPending()) {
            throw new com.example.paymentservice.exception.InvalidPaymentStateException(
                    orderId, status, "fail");
        }
        this.status = PaymentStatus.FAILED;
    }

    public void refund() {
        if (!isCompleted()) {
            throw new com.example.paymentservice.exception.InvalidPaymentStateException(
                    orderId, status, "refund");
        }
        this.status = PaymentStatus.REFUNDED;
    }
} 