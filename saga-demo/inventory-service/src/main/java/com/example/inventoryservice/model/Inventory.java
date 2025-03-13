package com.example.inventoryservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity class representing a product in inventory.
 */
@Entity
@Table(name = "inventory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "product_id", nullable = false, unique = true)
    private String productId;
    
    @Column(name = "product_name", nullable = false)
    private String productName;
    
    @Column(name = "available_quantity", nullable = false)
    private Integer availableQuantity;
    
    @Column(name = "reserved_quantity", nullable = false)
    private Integer reservedQuantity;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * Check if there is enough inventory available for the requested quantity
     * 
     * @param quantity The requested quantity
     * @return True if there is enough inventory, false otherwise
     */
    public boolean hasEnoughInventory(int quantity) {
        return availableQuantity >= quantity;
    }
    
    /**
     * Reserve inventory by decreasing available quantity and increasing reserved quantity
     * 
     * @param quantity The quantity to reserve
     * @throws com.example.inventoryservice.exception.InsufficientInventoryException if not enough inventory is available
     */
    public void reserve(int quantity) {
        if (availableQuantity < quantity) {
            throw new com.example.inventoryservice.exception.InsufficientInventoryException(
                    productId, quantity, availableQuantity);
        }
        availableQuantity -= quantity;
        reservedQuantity += quantity;
    }
    
    /**
     * Release reserved inventory by increasing available quantity and decreasing reserved quantity
     * 
     * @param quantity The quantity to release
     * @throws com.example.inventoryservice.exception.InventoryException if trying to release more than reserved
     */
    public void release(int quantity) {
        if (reservedQuantity < quantity) {
            throw new com.example.inventoryservice.exception.InventoryException(
                    "Cannot release more quantity (" + quantity + ") than reserved (" + reservedQuantity + ")");
        }
        reservedQuantity -= quantity;
        availableQuantity += quantity;
    }
    
    /**
     * Commit reserved inventory by decreasing reserved quantity
     * 
     * @param quantity The quantity to commit
     * @throws com.example.inventoryservice.exception.InventoryException if trying to confirm more than reserved
     */
    public void confirmReservation(int quantity) {
        if (reservedQuantity < quantity) {
            throw new com.example.inventoryservice.exception.InventoryException(
                    "Cannot confirm more quantity (" + quantity + ") than reserved (" + reservedQuantity + ")");
        }
        reservedQuantity -= quantity;
    }
    
    /**
     * Pre-persist hook to set creation and update timestamps
     */
    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Pre-update hook to set update timestamp
     */
    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
} 