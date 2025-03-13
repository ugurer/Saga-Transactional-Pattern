package com.example.paymentservice.service.impl;

import com.example.paymentservice.model.Outbox;
import com.example.paymentservice.repository.OutboxRepository;
import com.example.paymentservice.service.OutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxServiceImpl implements OutboxService {

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Override
    @Transactional
    public Long saveEvent(String aggregateType, String aggregateId, String eventType, String payload) {
        log.debug("Saving outbox event: type={}, aggregateId={}, eventType={}", 
                aggregateType, aggregateId, eventType);
        
        Outbox outbox = Outbox.builder()
                .aggregateType(aggregateType)
                .aggregateId(aggregateId)
                .eventType(eventType)
                .payload(payload)
                .build();
        
        outboxRepository.save(outbox);
        log.debug("Saved outbox event with ID: {}", outbox.getId());
        
        return outbox.getId();
    }

    @Override
    @Transactional
    public void processOutboxEvents() {
        // This would normally be called by a scheduled task
        // In a real application, we might want to process outbox events in batches
        // or implement a more sophisticated mechanism for ensuring delivery
        
        log.debug("Processing outbox events");
        List<Outbox> events = outboxRepository.findByCreatedAtBefore(LocalDateTime.now());
        
        for (Outbox event : events) {
            try {
                // Send to Kafka
                // In a real application, we would construct the topic name more carefully
                // and perhaps use more sophisticated routing
                String topic = event.getEventType().toLowerCase().replace('_', '-');
                
                log.debug("Sending outbox event to Kafka: id={}, topic={}", event.getId(), topic);
                kafkaTemplate.send(topic, event.getAggregateId(), event.getPayload());
                
                // In a real application with Debezium, we wouldn't need to manually publish to Kafka
                // Instead, Debezium would read the outbox table changes from the database WAL
                // and publish them to Kafka for us
                
                // Delete after processing (uncomment if needed)
                // outboxRepository.delete(event);
            } catch (Exception e) {
                log.error("Error processing outbox event id={}: {}", event.getId(), e.getMessage(), e);
                // In a real application, we might want to track failed events or implement a retry mechanism
            }
        }
        
        log.debug("Processed {} outbox events", events.size());
    }
} 