package com.example.inventoryservice.repository;

import com.example.inventoryservice.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for the Inventory entity.
 */
@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    
    /**
     * Find inventory by product ID.
     * 
     * @param productId The product ID to search for
     * @return An Optional containing the inventory if found, or empty if not found
     */
    Optional<Inventory> findByProductId(String productId);
    
    List<Inventory> findByProductIdIn(List<String> productIds);
    
    @Query("SELECT i FROM Inventory i WHERE i.availableQuantity >= :quantity AND i.productId = :productId")
    Optional<Inventory> findByProductIdWithSufficientQuantity(String productId, Integer quantity);
    
    @Query("SELECT CASE WHEN COUNT(i) > 0 THEN true ELSE false END FROM Inventory i WHERE i.productId = :productId AND i.availableQuantity >= :quantity")
    boolean hasEnoughInventory(String productId, Integer quantity);
} 