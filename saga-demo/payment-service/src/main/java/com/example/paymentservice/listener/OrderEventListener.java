package com.example.paymentservice.listener;

import com.example.paymentservice.event.OrderEvent;
import com.example.paymentservice.service.PaymentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "order-created", groupId = "${spring.kafka.consumer.group-id}")
    public void handleOrderCreated(@Payload String payload) {
        log.info("Received order-created event: {}", payload);
        try {
            OrderEvent event = objectMapper.readValue(payload, OrderEvent.class);
            
            // Process payment for the order
            paymentService.processPayment(
                    event.getOrderId(),
                    event.getTotalAmount(),
                    "CREDIT_CARD" // Default payment method for simplicity
            );
            
        } catch (JsonProcessingException e) {
            log.error("Error deserializing order-created event: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error handling order-created event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "order-cancelled", groupId = "${spring.kafka.consumer.group-id}")
    public void handleOrderCancelled(@Payload String payload) {
        log.info("Received order-cancelled event: {}", payload);
        try {
            OrderEvent event = objectMapper.readValue(payload, OrderEvent.class);
            
            // Refund payment for the order
            paymentService.refundPayment(event.getOrderId());
            
        } catch (JsonProcessingException e) {
            log.error("Error deserializing order-cancelled event: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error handling order-cancelled event: {}", e.getMessage(), e);
        }
    }
} 