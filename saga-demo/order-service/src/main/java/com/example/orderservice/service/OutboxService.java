package com.example.orderservice.service;

import com.example.orderservice.event.OrderEvent;

/**
 * Service interface for outbox operations.
 * This is used to implement the Outbox Pattern for reliable event publishing.
 */
public interface OutboxService {
    
    /**
     * Save an order event to the outbox
     * 
     * @param event The order event
     * @param aggregateType The aggregate type (e.g., "Order")
     * @param aggregateId The aggregate ID (e.g., the order ID)
     */
    void saveOrderEvent(OrderEvent event, String aggregateType, String aggregateId);
} 