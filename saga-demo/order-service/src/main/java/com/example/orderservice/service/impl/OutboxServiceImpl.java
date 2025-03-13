package com.example.orderservice.service.impl;

import com.example.orderservice.event.OrderEvent;
import com.example.orderservice.model.OutboxEvent;
import com.example.orderservice.repository.OutboxEventRepository;
import com.example.orderservice.service.OutboxService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Implementation of the OutboxService interface.
 * This service is responsible for saving events to the outbox table.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxServiceImpl implements OutboxService {
    
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void saveOrderEvent(OrderEvent event, String aggregateType, String aggregateId) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            
            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .id(UUID.randomUUID())
                    .aggregateType(aggregateType)
                    .aggregateId(aggregateId)
                    .eventType(event.getEventType())
                    .payload(payload)
                    .build();
            
            outboxEventRepository.save(outboxEvent);
            log.info("Saved event to outbox: {}, aggregateId: {}", event.getEventType(), aggregateId);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to serialize event", e);
        }
    }
} 