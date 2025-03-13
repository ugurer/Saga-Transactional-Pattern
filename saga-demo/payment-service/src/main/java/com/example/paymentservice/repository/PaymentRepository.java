package com.example.paymentservice.repository;

import com.example.paymentservice.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for the Payment entity.
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    /**
     * Find payment by order ID.
     * 
     * @param orderId The order ID to search for
     * @return An Optional containing the payment if found, or empty if not found
     */
    Optional<Payment> findByOrderId(String orderId);
    
    boolean existsByOrderId(String orderId);
} 