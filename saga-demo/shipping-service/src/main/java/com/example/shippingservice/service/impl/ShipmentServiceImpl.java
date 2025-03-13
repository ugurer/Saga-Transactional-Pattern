package com.example.shippingservice.service.impl;

import com.example.shippingservice.event.ShipmentEvent;
import com.example.shippingservice.exception.InvalidShipmentStateException;
import com.example.shippingservice.exception.ResourceNotFoundException;
import com.example.shippingservice.exception.ShipmentException;
import com.example.shippingservice.model.Shipment;
import com.example.shippingservice.repository.ShipmentRepository;
import com.example.shippingservice.service.OutboxService;
import com.example.shippingservice.service.ShipmentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of the ShipmentService interface.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ShipmentServiceImpl implements ShipmentService {
    
    private final ShipmentRepository shipmentRepository;
    private final OutboxService outboxService;
    private final ObjectMapper objectMapper;
    
    @Override
    @Transactional
    public Shipment createShipment(String orderId, String recipientName, String shippingAddress) {
        log.info("Creating shipment for order: {}", orderId);
        
        if (shipmentRepository.existsByOrderId(orderId)) {
            log.warn("Shipment already exists for order: {}", orderId);
            return shipmentRepository.findByOrderId(orderId);
        }
        
        Shipment shipment = Shipment.builder()
                .orderId(orderId)
                .recipientName(recipientName)
                .shippingAddress(shippingAddress)
                .status(Shipment.ShipmentStatus.PENDING)
                .build();
        
        shipmentRepository.save(shipment);
        log.info("Created shipment with ID: {} for order: {}", shipment.getId(), orderId);
        
        // Publish event via outbox
        publishShipmentEvent(shipment, ShipmentEvent.EventType.SHIPMENT_CREATED);
        
        return shipment;
    }
    
    @Override
    @Transactional
    public Shipment prepareShipment(String orderId) {
        log.info("Preparing shipment for order: {}", orderId);
        
        Shipment shipment = getShipmentByOrderId(orderId);
        shipment.prepare();
        shipmentRepository.save(shipment);
        
        log.info("Shipment for order {} is now in PREPARING state", orderId);
        
        // Publish event via outbox
        publishShipmentEvent(shipment, ShipmentEvent.EventType.SHIPMENT_PREPARING);
        
        return shipment;
    }
    
    @Override
    @Transactional
    public Shipment shipOrder(String orderId) {
        log.info("Shipping order: {}", orderId);
        
        Shipment shipment = getShipmentByOrderId(orderId);
        
        // Generate a tracking number if not already present
        if (shipment.getTrackingNumber() == null || shipment.getTrackingNumber().isEmpty()) {
            shipment.setTrackingNumber("TRK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        
        shipment.ship();
        shipmentRepository.save(shipment);
        
        log.info("Shipment for order {} is now SHIPPED with tracking number: {}", 
                orderId, shipment.getTrackingNumber());
        
        // Publish event via outbox
        publishShipmentEvent(shipment, ShipmentEvent.EventType.SHIPMENT_SHIPPED);
        
        return shipment;
    }
    
    @Override
    @Transactional
    public Shipment deliverOrder(String orderId) {
        log.info("Marking order as delivered: {}", orderId);
        
        Shipment shipment = getShipmentByOrderId(orderId);
        shipment.deliver();
        shipmentRepository.save(shipment);
        
        log.info("Shipment for order {} is now DELIVERED", orderId);
        
        // Publish event via outbox
        publishShipmentEvent(shipment, ShipmentEvent.EventType.SHIPMENT_DELIVERED);
        
        return shipment;
    }
    
    @Override
    @Transactional
    public Shipment cancelShipment(String orderId, String reason) {
        log.info("Cancelling shipment for order: {}, reason: {}", orderId, reason);
        
        Shipment shipment = getShipmentByOrderId(orderId);
        shipment.cancel();
        shipment.setCancellationReason(reason);
        shipmentRepository.save(shipment);
        
        log.info("Shipment for order {} is now CANCELLED", orderId);
        
        // Publish event via outbox
        publishShipmentEvent(shipment, ShipmentEvent.EventType.SHIPMENT_CANCELLED);
        
        return shipment;
    }
    
    @Override
    public Shipment getShipmentByOrderId(String orderId) {
        return shipmentRepository.findByOrderId(orderId)
                .orElseThrow(() -> {
                    log.error("Shipment not found for order: {}", orderId);
                    return new IllegalArgumentException("Shipment not found for order: " + orderId);
                });
    }
    
    @Override
    public List<Shipment> findAll() {
        return shipmentRepository.findAll();
    }
    
    private void publishShipmentEvent(Shipment shipment, ShipmentEvent.EventType eventType) {
        try {
            ShipmentEvent event = ShipmentEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .orderId(shipment.getOrderId())
                    .eventType(eventType)
                    .timestamp(LocalDateTime.now())
                    .recipientName(shipment.getRecipientName())
                    .shippingAddress(shipment.getShippingAddress())
                    .trackingNumber(shipment.getTrackingNumber())
                    .build();
            
            String payload = objectMapper.writeValueAsString(event);
            
            outboxService.saveEvent(
                    "Shipment",
                    shipment.getOrderId(),
                    eventType.name(),
                    payload
            );
            
            log.debug("Published shipment event: {}", eventType);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize shipment event: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to publish shipment event", e);
        }
    }
} 