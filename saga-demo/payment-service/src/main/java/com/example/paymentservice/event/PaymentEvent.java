package com.example.paymentservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Event class for payment-related events.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentEvent {
    
    /**
     * Type of the payment event.
     */
    public enum EventType {
        PAYMENT_COMPLETED,
        PAYMENT_FAILED,
        PAYMENT_REFUNDED
    }
    
    /**
     * Unique identifier for the event.
     */
    private String eventId;
    
    /**
     * Order ID associated with this event.
     */
    private String orderId;
    
    /**
     * Type of the event.
     */
    private EventType eventType;
    
    /**
     * Timestamp when the event was created.
     */
    private LocalDateTime timestamp;
    
    /**
     * Payment amount.
     */
    private BigDecimal amount;
    
    /**
     * Transaction ID (if payment was successful).
     */
    private String transactionId;
    
    /**
     * Reason for the event.
     */
    private String reason;
} 