package com.example.inventoryservice.service.impl;

import com.example.inventoryservice.event.InventoryEvent;
import com.example.inventoryservice.exception.InventoryException;
import com.example.inventoryservice.exception.InsufficientInventoryException;
import com.example.inventoryservice.exception.ResourceNotFoundException;
import com.example.inventoryservice.model.Inventory;
import com.example.inventoryservice.repository.InventoryRepository;
import com.example.inventoryservice.service.InventoryService;
import com.example.inventoryservice.service.OutboxService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of the InventoryService interface.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService {
    
    private final InventoryRepository inventoryRepository;
    private final OutboxService outboxService;
    private final ObjectMapper objectMapper;
    
    @Override
    public Inventory findByProductId(String productId) {
        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "productId", productId));
    }
    
    @Override
    public List<Inventory> findByProductIds(List<String> productIds) {
        List<Inventory> inventories = inventoryRepository.findByProductIdIn(productIds);
        if (inventories.size() != productIds.size()) {
            List<String> foundProductIds = inventories.stream()
                    .map(Inventory::getProductId)
                    .collect(Collectors.toList());
            
            List<String> missingProductIds = productIds.stream()
                    .filter(id -> !foundProductIds.contains(id))
                    .collect(Collectors.toList());
            
            throw new ResourceNotFoundException("Some products were not found: " + String.join(", ", missingProductIds));
        }
        return inventories;
    }
    
    @Override
    public boolean hasEnoughInventory(String productId, int quantity) {
        return inventoryRepository.hasEnoughInventory(productId, quantity);
    }
    
    @Override
    public Map<String, Boolean> checkInventoryAvailability(Map<String, Integer> productQuantities) {
        Map<String, Boolean> result = new HashMap<>();
        
        for (Map.Entry<String, Integer> entry : productQuantities.entrySet()) {
            result.put(entry.getKey(), hasEnoughInventory(entry.getKey(), entry.getValue()));
        }
        
        return result;
    }
    
    @Override
    @Transactional
    public void reserveInventory(String orderId, Map<String, Integer> productQuantities) {
        log.info("Reserving inventory for order {}: {}", orderId, productQuantities);
        
        // First check if all products have sufficient inventory
        for (Map.Entry<String, Integer> entry : productQuantities.entrySet()) {
            String productId = entry.getKey();
            Integer quantity = entry.getValue();
            
            if (!hasEnoughInventory(productId, quantity)) {
                Inventory inventory = findByProductId(productId);
                String reason = String.format("Insufficient inventory for product %s. Requested: %d, Available: %d",
                        productId, quantity, inventory.getAvailableQuantity());
                
                // Create failure event
                publishOutboxEvent(
                        InventoryEvent.builder()
                                .eventId(UUID.randomUUID().toString())
                                .eventType(InventoryEvent.EventType.INVENTORY_RESERVATION_FAILED)
                                .orderId(orderId)
                                .productId(productId)
                                .quantity(quantity)
                                .reason(reason)
                                .timestamp(LocalDateTime.now())
                                .build()
                );
                
                throw new InsufficientInventoryException(productId, quantity, inventory.getAvailableQuantity());
            }
        }
        
        // If we get here, all products have sufficient inventory
        // Now reserve each product
        for (Map.Entry<String, Integer> entry : productQuantities.entrySet()) {
            String productId = entry.getKey();
            Integer quantity = entry.getValue();
            
            Inventory inventory = findByProductId(productId);
            inventory.reserve(quantity);
            inventoryRepository.save(inventory);
            
            // Create success event
            publishOutboxEvent(
                    InventoryEvent.builder()
                            .eventId(UUID.randomUUID().toString())
                            .eventType(InventoryEvent.EventType.INVENTORY_RESERVED)
                            .orderId(orderId)
                            .productId(productId)
                            .quantity(quantity)
                            .timestamp(LocalDateTime.now())
                            .build()
            );
        }
        
        log.info("Inventory reserved successfully for order {}", orderId);
    }
    
    /**
     * Release inventory reservation as a Compensation operation.
     * This method is used as a compensation action in case of errors within the SAGA pattern.
     * 
     * It's called when an order is canceled or when errors occur in payment/shipping steps.
     * It makes previously reserved stocks available again.
     */
    @Override
    @Transactional
    public void releaseInventory(String orderId, Map<String, Integer> productQuantities) {
        log.info("Starting compensation process: Canceling stock reservation for order {}", orderId);
        
        if (productQuantities == null || productQuantities.isEmpty()) {
            log.warn("Skipping compensation: Product list is empty or null. OrderId: {}", orderId);
            return;
        }
        
        try {
            List<String> productIds = productQuantities.keySet().stream().collect(Collectors.toList());
            List<Inventory> inventories = findByProductIds(productIds);
            
            log.debug("Compensation process: {} products will have their stock released", inventories.size());
            
            for (Inventory inventory : inventories) {
                String productId = inventory.getProductId();
                int quantity = productQuantities.get(productId);
                
                // Decrease the product's reserved quantity and increase available quantity
                inventory.setReservedQuantity(inventory.getReservedQuantity() - quantity);
                inventory.setAvailableQuantity(inventory.getAvailableQuantity() + quantity);
                
                log.debug("Stock compensated for product {}: Reserved: {}, Available: {}", 
                        productId, inventory.getReservedQuantity(), inventory.getAvailableQuantity());
                
                inventoryRepository.save(inventory);
            }
            
            // Publish InventoryReleased event when stock release is successful
            InventoryEvent event = InventoryEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("InventoryReleased")
                    .orderId(orderId)
                    .productQuantities(productQuantities)
                    .timestamp(LocalDateTime.now())
                    .build();
            
            publishOutboxEvent(event);
            log.info("Compensation successful: Stock released for order {} and InventoryReleased event published", orderId);
        } catch (Exception e) {
            log.error("Error during compensation: Could not release stock for order {}: {}", orderId, e.getMessage(), e);
            throw new InventoryException("Error occurred while releasing stock for order " + orderId, e);
        }
    }
    
    @Override
    @Transactional
    public void confirmInventory(String orderId, Map<String, Integer> productQuantities) {
        log.info("Confirming inventory for order {}: {}", orderId, productQuantities);
        
        for (Map.Entry<String, Integer> entry : productQuantities.entrySet()) {
            String productId = entry.getKey();
            Integer quantity = entry.getValue();
            
            try {
                Inventory inventory = findByProductId(productId);
                inventory.confirmReservation(quantity);
                inventoryRepository.save(inventory);
                
                // Create event
                publishOutboxEvent(
                        InventoryEvent.builder()
                                .eventId(UUID.randomUUID().toString())
                                .eventType(InventoryEvent.EventType.INVENTORY_CONFIRMED)
                                .orderId(orderId)
                                .productId(productId)
                                .quantity(quantity)
                                .timestamp(LocalDateTime.now())
                                .build()
                );
                
            } catch (Exception e) {
                log.error("Error confirming inventory for productId={}, quantity={}: {}", 
                        productId, quantity, e.getMessage(), e);
                throw new InventoryException("Failed to confirm inventory: " + e.getMessage(), e);
            }
        }
        
        log.info("Inventory confirmed successfully for order {}", orderId);
    }
    
    private void publishOutboxEvent(InventoryEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            outboxService.saveEvent(
                    "inventory",
                    event.getProductId(),
                    event.getEventType().name(),
                    payload
            );
        } catch (JsonProcessingException e) {
            log.error("Error serializing inventory event: {}", e.getMessage(), e);
            throw new InventoryException("Failed to create outbox event: " + e.getMessage(), e);
        }
    }
} 