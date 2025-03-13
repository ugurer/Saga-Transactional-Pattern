package com.example.paymentservice.service;

import com.example.paymentservice.model.Payment;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for payment operations.
 */
public interface PaymentService {
    
    /**
     * Get all payments.
     * 
     * @return List of all payments
     */
    List<Payment> getAllPayments();
    
    /**
     * Get payment by ID.
     * 
     * @param id The payment ID
     * @return Optional containing the payment if found, empty otherwise
     */
    Optional<Payment> getPaymentById(Long id);
    
    /**
     * Get payment by order ID.
     * 
     * @param orderId The order ID
     * @return Optional containing the payment if found, empty otherwise
     */
    Optional<Payment> getPaymentByOrderId(String orderId);
    
    /**
     * Create a new payment.
     * 
     * @param payment The payment to create
     * @return The created payment
     */
    Payment createPayment(Payment payment);
    
    /**
     * Process a payment for an order.
     * 
     * @param orderId Order ID
     * @param amount Payment amount
     * @param paymentMethod Payment method (e.g., "CREDIT_CARD", "PAYPAL")
     * @return Payment object
     */
    Payment processPayment(String orderId, BigDecimal amount, String paymentMethod);
    
    /**
     * Refund a payment for an order.
     *
     * @param orderId Order ID
     * @return Refunded payment
     */
    Payment refundPayment(String orderId);
    
    /**
     * Find payment by order ID.
     *
     * @param orderId Order ID
     * @return Payment if found
     */
    Payment findByOrderId(String orderId);
    
    /**
     * Find all payments.
     *
     * @return List of all payments
     */
    List<Payment> findAll();
} 