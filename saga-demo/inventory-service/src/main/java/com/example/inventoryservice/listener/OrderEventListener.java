package com.example.inventoryservice.listener;

import com.example.inventoryservice.event.OrderEvent;
import com.example.inventoryservice.service.InventoryService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Kafka listener for order-related events.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {
    
    private final InventoryService inventoryService;
    private final ObjectMapper objectMapper;
    
    @KafkaListener(topics = "order-created", groupId = "${spring.kafka.consumer.group-id}")
    public void handleOrderCreated(@Payload String payload) {
        log.info("Received order-created event: {}", payload);
        try {
            OrderEvent event = objectMapper.readValue(payload, OrderEvent.class);
            
            // Extract product quantities from order
            Map<String, Integer> productQuantities = event.getOrderItems().stream()
                    .collect(Collectors.toMap(
                            OrderEvent.OrderItem::getProductId,
                            OrderEvent.OrderItem::getQuantity
                    ));
            
            // Attempt to reserve inventory
            inventoryService.reserveInventory(event.getOrderId(), productQuantities);
            
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
            
            // Extract product quantities from order
            Map<String, Integer> productQuantities = event.getOrderItems().stream()
                    .collect(Collectors.toMap(
                            OrderEvent.OrderItem::getProductId,
                            OrderEvent.OrderItem::getQuantity
                    ));
            
            // Release reserved inventory
            inventoryService.releaseInventory(event.getOrderId(), productQuantities);
            
        } catch (JsonProcessingException e) {
            log.error("Error deserializing order-cancelled event: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error handling order-cancelled event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "order-completed", groupId = "${spring.kafka.consumer.group-id}")
    public void handleOrderCompleted(@Payload String payload) {
        log.info("Received order-completed event: {}", payload);
        try {
            OrderEvent event = objectMapper.readValue(payload, OrderEvent.class);
            
            // Extract product quantities from order
            Map<String, Integer> productQuantities = event.getOrderItems().stream()
                    .collect(Collectors.toMap(
                            OrderEvent.OrderItem::getProductId,
                            OrderEvent.OrderItem::getQuantity
                    ));
            
            // Confirm reserved inventory
            inventoryService.confirmInventory(event.getOrderId(), productQuantities);
            
        } catch (JsonProcessingException e) {
            log.error("Error deserializing order-completed event: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error handling order-completed event: {}", e.getMessage(), e);
        }
    }
} 