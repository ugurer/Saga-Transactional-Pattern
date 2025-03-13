package com.example.shippingservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentEvent {
    
    public enum EventType {
        PAYMENT_COMPLETED,
        PAYMENT_FAILED,
        PAYMENT_REFUNDED
    }

    private String eventId;
    private String orderId;
    private EventType eventType;
    private LocalDateTime timestamp;
    private BigDecimal amount;
    private String transactionId;
    private String reason;
} 