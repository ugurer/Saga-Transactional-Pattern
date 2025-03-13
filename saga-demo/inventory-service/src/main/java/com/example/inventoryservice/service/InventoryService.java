package com.example.inventoryservice.service;

import com.example.inventoryservice.model.Inventory;

import java.util.List;
import java.util.Map;

/**
 * Service interface for inventory operations.
 */
public interface InventoryService {
    
    /**
     * Get all inventory items.
     * 
     * @return List of all inventory items
     */
    List<Inventory> getAllInventory();
    
    /**
     * Get inventory by ID.
     * 
     * @param id The inventory ID
     * @return Optional containing the inventory if found, empty otherwise
     */
    Inventory getInventoryById(Long id);
    
    /**
     * Get inventory by product ID.
     * 
     * @param productId The product ID
     * @return Optional containing the inventory if found, empty otherwise
     */
    Inventory getInventoryByProductId(String productId);
    
    /**
     * Create or update an inventory item.
     * 
     * @param inventory The inventory item to save
     * @return The saved inventory item
     */
    Inventory saveInventory(Inventory inventory);
    
    /**
     * Delete an inventory item by ID.
     * 
     * @param id The ID of the inventory to delete
     */
    void deleteInventory(Long id);
    
    /**
     * Find inventory by product ID.
     */
    Inventory findByProductId(String productId);
    
    /**
     * Find multiple inventory items by product IDs.
     */
    List<Inventory> findByProductIds(List<String> productIds);
    
    /**
     * Check if there is sufficient inventory for the product.
     */
    boolean hasEnoughInventory(String productId, int quantity);
    
    /**
     * Check if there is sufficient inventory for multiple products.
     * Returns a map of product IDs to boolean indicating sufficient inventory.
     */
    Map<String, Boolean> checkInventoryAvailability(Map<String, Integer> productQuantities);
    
    /**
     * Reserve inventory for an order.
     * Reduces available quantity and increases reserved quantity.
     */
    void reserveInventory(String orderId, Map<String, Integer> productQuantities);
    
    /**
     * Release previously reserved inventory.
     * Reduces reserved quantity and increases available quantity.
     */
    void releaseInventory(String orderId, Map<String, Integer> productQuantities);
    
    /**
     * Confirm inventory reservation (e.g., after order is completed).
     * Reduces reserved quantity.
     */
    void confirmInventory(String orderId, Map<String, Integer> productQuantities);
} 