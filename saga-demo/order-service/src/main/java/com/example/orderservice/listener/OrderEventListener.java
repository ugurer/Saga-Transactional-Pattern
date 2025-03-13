package com.example.orderservice.listener;

import com.example.orderservice.model.OrderStatus;
import com.example.orderservice.service.OrderService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka listener for order-related events from other services.
 * This is part of the SAGA pattern implementation.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {
    
    private final OrderService orderService;
    private final ObjectMapper objectMapper;
    
    /**
     * Listen for inventory events
     * 
     * @param message The Kafka message
     */
    @KafkaListener(topics = "inventory-events", groupId = "order-service-group")
    public void handleInventoryEvents(String message) {
        try {
            log.info("Received inventory event: {}", message);
            JsonNode eventNode = objectMapper.readTree(message);
            
            String eventType = eventNode.path("eventType").asText();
            Long orderId = eventNode.path("orderId").asLong();
            
            switch (eventType) {
                case "InventoryReserved":
                    log.info("Inventory reserved for order: {}", orderId);
                    // No action needed, wait for payment event
                    break;
                case "InventoryReservationFailed":
                    log.info("Inventory reservation failed for order: {}", orderId);
                    orderService.updateOrderStatus(orderId, OrderStatus.FAILED);
                    break;
                default:
                    log.warn("Unknown inventory event type: {}", eventType);
            }
        } catch (JsonProcessingException e) {
            log.error("Error processing inventory event: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Listen for payment events
     * 
     * @param message The Kafka message
     */
    @KafkaListener(topics = "payment-events", groupId = "order-service-group")
    public void handlePaymentEvents(String message) {
        try {
            log.info("Received payment event: {}", message);
            JsonNode eventNode = objectMapper.readTree(message);
            
            String eventType = eventNode.path("eventType").asText();
            Long orderId = eventNode.path("orderId").asLong();
            
            switch (eventType) {
                case "PaymentCompleted":
                    log.info("Payment completed for order: {}", orderId);
                    orderService.updateOrderStatus(orderId, OrderStatus.CONFIRMED);
                    break;
                case "PaymentFailed":
                    log.info("Payment failed for order: {}", orderId);
                    orderService.updateOrderStatus(orderId, OrderStatus.FAILED);
                    break;
                default:
                    log.warn("Unknown payment event type: {}", eventType);
            }
        } catch (JsonProcessingException e) {
            log.error("Error processing payment event: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Listen for shipping events
     * 
     * @param message The Kafka message
     */
    @KafkaListener(topics = "shipping-events", groupId = "order-service-group")
    public void handleShippingEvents(String message) {
        try {
            log.info("Received shipping event: {}", message);
            JsonNode eventNode = objectMapper.readTree(message);
            
            String eventType = eventNode.path("eventType").asText();
            Long orderId = eventNode.path("orderId").asLong();
            
            switch (eventType) {
                case "OrderShipped":
                    log.info("Order shipped: {}", orderId);
                    orderService.updateOrderStatus(orderId, OrderStatus.COMPLETED);
                    break;
                case "ShippingFailed":
                    log.info("Shipping failed for order: {}", orderId);
                    // This is a critical failure after payment, may need manual intervention
                    // For now, mark as failed but in a real system might need special handling
                    orderService.updateOrderStatus(orderId, OrderStatus.FAILED);
                    break;
                default:
                    log.warn("Unknown shipping event type: {}", eventType);
            }
        } catch (JsonProcessingException e) {
            log.error("Error processing shipping event: {}", e.getMessage(), e);
        }
    }
} 