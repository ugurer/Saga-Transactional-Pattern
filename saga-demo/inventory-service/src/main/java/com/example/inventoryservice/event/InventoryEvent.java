package com.example.inventoryservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Event class for inventory-related events.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryEvent {
    
    /**
     * Type of the inventory event.
     */
    public enum EventType {
        INVENTORY_RESERVED,
        INVENTORY_RESERVATION_FAILED,
        INVENTORY_RELEASED,
        INVENTORY_CONFIRMED
    }
    
    /**
     * Unique identifier for the event.
     */
    private String eventId;
    
    /**
     * Product ID for which inventory is being modified.
     */
    private String productId;
    
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
     * Quantity being reserved, released, or committed.
     */
    private Integer quantity;
    
    /**
     * Additional message providing details about the event.
     */
    private String reason;
} 