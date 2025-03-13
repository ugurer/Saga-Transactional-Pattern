package com.example.shippingservice.repository;

import com.example.shippingservice.model.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for the Shipment entity.
 */
@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    
    /**
     * Find shipment by order ID.
     * 
     * @param orderId The order ID to search for
     * @return An Optional containing the shipment if found, or empty if not found
     */
    Optional<Shipment> findByOrderId(String orderId);
    
    boolean existsByOrderId(String orderId);
} 