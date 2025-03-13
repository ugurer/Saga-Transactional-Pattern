package com.example.inventoryservice.controller;

import com.example.inventoryservice.model.Inventory;
import com.example.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for inventory operations.
 */
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Slf4j
public class InventoryController {
    
    private final InventoryService inventoryService;
    
    /**
     * Get all inventory items.
     * 
     * @return List of all inventory items
     */
    @GetMapping
    public ResponseEntity<List<Inventory>> getAllInventory() {
        return ResponseEntity.ok(inventoryService.getAllInventory());
    }
    
    /**
     * Get inventory by ID.
     * 
     * @param id The inventory ID
     * @return The inventory item if found, or 404 Not Found
     */
    @GetMapping("/{id}")
    public ResponseEntity<Inventory> getInventoryById(@PathVariable Long id) {
        return inventoryService.getInventoryById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get inventory by product ID.
     * 
     * @param productId The product ID
     * @return The inventory item if found, or 404 Not Found
     */
    @GetMapping("/{productId}")
    public ResponseEntity<Inventory> getInventoryByProductId(@PathVariable String productId) {
        log.info("Fetching inventory for product ID: {}", productId);
        return ResponseEntity.ok(inventoryService.findByProductId(productId));
    }
    
    /**
     * Create a new inventory item.
     * 
     * @param inventory The inventory item to create
     * @return The created inventory item
     */
    @PostMapping
    public ResponseEntity<Inventory> createInventory(@RequestBody Inventory inventory) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inventoryService.saveInventory(inventory));
    }
    
    /**
     * Update an existing inventory item.
     * 
     * @param id The inventory ID
     * @param inventory The updated inventory data
     * @return The updated inventory or 404 Not Found
     */
    @PutMapping("/{id}")
    public ResponseEntity<Inventory> updateInventory(@PathVariable Long id, @RequestBody Inventory inventory) {
        return inventoryService.getInventoryById(id)
                .map(existingInventory -> {
                    inventory.setId(id);
                    return ResponseEntity.ok(inventoryService.saveInventory(inventory));
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Delete an inventory item.
     * 
     * @param id The inventory ID
     * @return 204 No Content on success, 404 Not Found if not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInventory(@PathVariable Long id) {
        return inventoryService.getInventoryById(id)
                .map(inventory -> {
                    inventoryService.deleteInventory(id);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Check inventory availability.
     * 
     * @param productId The product ID
     * @param quantity The quantity to check
     * @return True if inventory is available, false otherwise
     */
    @GetMapping("/check/{productId}")
    public ResponseEntity<Boolean> checkInventory(
            @PathVariable String productId,
            @RequestParam(defaultValue = "1") int quantity) {
        log.info("Checking inventory for product ID: {}, quantity: {}", productId, quantity);
        return ResponseEntity.ok(inventoryService.hasEnoughInventory(productId, quantity));
    }
    
    /**
     * Check inventory availability for multiple products.
     * 
     * @param productQuantities Map of product IDs and their quantities
     * @return Map of product IDs and their availability status
     */
    @PostMapping("/check")
    public ResponseEntity<Map<String, Boolean>> checkInventoryBulk(
            @RequestBody Map<String, Integer> productQuantities) {
        log.info("Checking inventory for multiple products: {}", productQuantities);
        return ResponseEntity.ok(inventoryService.checkInventoryAvailability(productQuantities));
    }
    
    /**
     * Get inventory items by product IDs.
     * 
     * @param productIds List of product IDs
     * @return List of inventory items
     */
    @GetMapping("/products")
    public ResponseEntity<List<Inventory>> getInventoryByProductIds(
            @RequestParam List<String> productIds) {
        log.info("Fetching inventory for product IDs: {}", productIds);
        return ResponseEntity.ok(inventoryService.findByProductIds(productIds));
    }
    
    /**
     * Reserve inventory.
     * 
     * @param orderId The order ID
     * @param productQuantities Map of product IDs and their quantities
     * @return 200 OK if successful, 400 Bad Request if failed
     */
    @PostMapping("/reserve")
    public ResponseEntity<Void> reserveInventory(
            @RequestParam String orderId,
            @RequestBody Map<String, Integer> productQuantities) {
        log.info("Reserving inventory for order ID: {}, products: {}", orderId, productQuantities);
        inventoryService.reserveInventory(orderId, productQuantities);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Release inventory.
     * 
     * @param orderId The order ID
     * @param productQuantities Map of product IDs and their quantities
     * @return 200 OK if successful, 400 Bad Request if failed
     */
    @PostMapping("/release")
    public ResponseEntity<Void> releaseInventory(
            @RequestParam String orderId,
            @RequestBody Map<String, Integer> productQuantities) {
        log.info("Releasing inventory for order ID: {}, products: {}", orderId, productQuantities);
        inventoryService.releaseInventory(orderId, productQuantities);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Confirm inventory.
     * 
     * @param orderId The order ID
     * @param productQuantities Map of product IDs and their quantities
     * @return 200 OK if successful, 400 Bad Request if failed
     */
    @PostMapping("/confirm")
    public ResponseEntity<Void> confirmInventory(
            @RequestParam String orderId,
            @RequestBody Map<String, Integer> productQuantities) {
        log.info("Confirming inventory for order ID: {}, products: {}", orderId, productQuantities);
        inventoryService.confirmInventory(orderId, productQuantities);
        return ResponseEntity.ok().build();
    }
} 