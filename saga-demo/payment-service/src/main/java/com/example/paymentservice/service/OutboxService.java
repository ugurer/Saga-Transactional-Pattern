package com.example.paymentservice.service;

public interface OutboxService {

    /**
     * Save an event to the outbox table.
     * 
     * @param aggregateType The type of the aggregate (e.g., "payment")
     * @param aggregateId The ID of the aggregate (e.g., order ID)
     * @param eventType The type of the event (e.g., "PAYMENT_COMPLETED")
     * @param payload The JSON payload of the event
     * @return The ID of the created outbox entry
     */
    Long saveEvent(String aggregateType, String aggregateId, String eventType, String payload);
    
    /**
     * Process outbox events that have not been processed yet.
     * This would typically be called by a scheduled task.
     */
    void processOutboxEvents();
} 