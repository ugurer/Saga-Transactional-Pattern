package com.example.inventoryservice.config;

import com.example.inventoryservice.service.OutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class SchedulingConfig {

    private final OutboxService outboxService;

    /**
     * Process outbox events every 5 seconds.
     * In a real application with Debezium, this would not be necessary.
     * Debezium would automatically capture the outbox events from the database.
     */
    @Scheduled(fixedRate = 5000)
    public void processOutboxEvents() {
        log.debug("Running scheduled outbox processing");
        outboxService.processOutboxEvents();
    }
} 