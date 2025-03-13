package com.example.shippingservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Event class for shipment-related events.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentEvent {
    
    /**
     * Type of the shipment event.
     */
    public enum EventType {
        SHIPMENT_CREATED,
        SHIPMENT_PREPARED,
        SHIPMENT_SHIPPED,
        SHIPMENT_DELIVERED,
        SHIPMENT_CANCELLED
    }
    
    /**
     * Unique identifier for the event.
     */
    private String eventId;
    
    /**
     * Type of the event.
     */
    private EventType eventType;
    
    /**
     * Order ID associated with this event.
     */
    private String orderId;
    
    /**
     * Recipient's name.
     */
    private String recipientName;
    
    /**
     * Shipping address.
     */
    private String shippingAddress;
    
    /**
     * Tracking number (if shipped).
     */
    private String trackingNumber;
    
    /**
     * Timestamp when the event was created.
     */
    private LocalDateTime timestamp;
    
    /**
     * Additional message providing details about the event.
     */
    private String reason;
} 