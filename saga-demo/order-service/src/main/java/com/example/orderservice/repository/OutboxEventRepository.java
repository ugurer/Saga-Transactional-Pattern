package com.example.orderservice.repository;

import com.example.orderservice.model.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for OutboxEvent entity operations.
 */
@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {
    
    /**
     * Find outbox events by event type
     * 
     * @param eventType The event type
     * @return List of outbox events with the given event type
     */
    List<OutboxEvent> findByEventType(String eventType);
    
    /**
     * Find outbox events by aggregate type and aggregate ID
     * 
     * @param aggregateType The aggregate type
     * @param aggregateId The aggregate ID
     * @return List of outbox events for the given aggregate
     */
    List<OutboxEvent> findByAggregateTypeAndAggregateId(String aggregateType, String aggregateId);
} 