package com.example.shippingservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity class representing a shipment.
 */
@Entity
@Table(name = "shipments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shipment {
    
    /**
     * Possible shipment statuses.
     */
    public enum ShipmentStatus {
        PENDING,
        PREPARING,
        SHIPPED,
        DELIVERED,
        CANCELLED
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "order_id", nullable = false, unique = true)
    private String orderId;
    
    @Column(name = "recipient_name", nullable = false)
    private String recipientName;
    
    @Column(name = "shipping_address", nullable = false)
    private String shippingAddress;
    
    @Column(name = "tracking_number")
    private String trackingNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ShipmentStatus status;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;
    
    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;
    
    @Column(name = "cancellation_reason")
    private String cancellationReason;
    
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
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Prepare shipment for shipping.
     */
    public void prepare() {
        if (status != ShipmentStatus.PENDING) {
            throw new IllegalStateException("Shipment can only be prepared from PENDING state. Current state: " + status);
        }
        status = ShipmentStatus.PREPARING;
    }
    
    /**
     * Mark shipment as shipped.
     */
    public void ship() {
        if (status != ShipmentStatus.PREPARING) {
            throw new IllegalStateException("Shipment can only be shipped from PREPARING state. Current state: " + status);
        }
        status = ShipmentStatus.SHIPPED;
        shippedAt = LocalDateTime.now();
    }
    
    /**
     * Mark shipment as delivered.
     */
    public void deliver() {
        if (status != ShipmentStatus.SHIPPED) {
            throw new IllegalStateException("Shipment can only be delivered from SHIPPED state. Current state: " + status);
        }
        status = ShipmentStatus.DELIVERED;
        deliveredAt = LocalDateTime.now();
    }
    
    /**
     * Cancel the shipment.
     */
    public void cancel() {
        if (status == ShipmentStatus.DELIVERED) {
            throw new IllegalStateException("Cannot cancel DELIVERED shipment.");
        }
        status = ShipmentStatus.CANCELLED;
    }
} 