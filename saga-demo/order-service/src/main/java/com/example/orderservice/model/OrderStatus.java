package com.example.orderservice.model;

/**
 * Enum representing the possible statuses of an order.
 */
public enum OrderStatus {
    /**
     * Order has been created but not yet processed
     */
    PENDING,
    
    /**
     * Order has been confirmed (inventory reserved, payment processed)
     */
    CONFIRMED,
    
    /**
     * Order has been fully processed (shipped and delivered)
     */
    COMPLETED,
    
    /**
     * Order has been cancelled
     */
    CANCELLED,
    
    /**
     * Order processing has failed
     */
    FAILED
} 