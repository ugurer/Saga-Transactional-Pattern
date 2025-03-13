package com.example.paymentservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryEvent {
    
    public enum EventType {
        INVENTORY_RESERVED,
        INVENTORY_RESERVATION_FAILED,
        INVENTORY_RELEASED,
        INVENTORY_CONFIRMED
    }

    private String eventId;
    private String productId;
    private String orderId;
    private EventType eventType;
    private LocalDateTime timestamp;
    private Integer quantity;
    private String reason;
} 