package com.example.paymentservice.service.impl;

import com.example.paymentservice.event.PaymentEvent;
import com.example.paymentservice.exception.InvalidPaymentStateException;
import com.example.paymentservice.exception.PaymentException;
import com.example.paymentservice.exception.PaymentProcessingException;
import com.example.paymentservice.exception.ResourceNotFoundException;
import com.example.paymentservice.model.Payment;
import com.example.paymentservice.repository.PaymentRepository;
import com.example.paymentservice.service.OutboxService;
import com.example.paymentservice.service.PaymentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of the PaymentService interface.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final OutboxService outboxService;
    private final ObjectMapper objectMapper;
    
    @Override
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }
    
    @Override
    public Optional<Payment> getPaymentById(Long id) {
        return paymentRepository.findById(id);
    }
    
    @Override
    public Optional<Payment> getPaymentByOrderId(String orderId) {
        return paymentRepository.findByOrderId(orderId);
    }
    
    @Override
    public Payment createPayment(Payment payment) {
        return paymentRepository.save(payment);
    }
    
    @Override
    @Transactional
    public Payment processPayment(String orderId, BigDecimal amount, String paymentMethod) {
        log.info("Processing payment for order {}, amount: {}, method: {}", orderId, amount, paymentMethod);
        
        // Check if payment already exists for this order
        if (paymentRepository.existsByOrderId(orderId)) {
            Payment existingPayment = findByOrderId(orderId);
            
            // If payment is completed, return it
            if (existingPayment.isCompleted()) {
                log.info("Payment for order {} already completed", orderId);
                return existingPayment;
            }
            
            // If payment is pending, we can continue and update it
            if (existingPayment.isPending()) {
                log.info("Found pending payment for order {}, will update", orderId);
                return processPaymentTransaction(existingPayment);
            }
            
            // Payment is in a state that doesn't allow processing
            throw new InvalidPaymentStateException(
                    orderId, existingPayment.getStatus(), "process");
        }
        
        // Create new payment
        Payment payment = Payment.builder()
                .orderId(orderId)
                .amount(amount)
                .paymentMethod(paymentMethod)
                .status(Payment.PaymentStatus.PENDING)
                .build();
        
        payment = paymentRepository.save(payment);
        
        return processPaymentTransaction(payment);
    }
    
    private Payment processPaymentTransaction(Payment payment) {
        try {
            // Simulate payment processing with external service
            // In a real application, this would call a payment gateway
            boolean paymentSuccess = simulatePaymentProcessing(payment);
            
            if (paymentSuccess) {
                // Generate a transaction ID
                String transactionId = UUID.randomUUID().toString();
                
                // Mark payment as completed
                payment.markAsCompleted(transactionId);
                payment = paymentRepository.save(payment);
                
                // Publish payment completed event
                publishOutboxEvent(
                        PaymentEvent.builder()
                                .eventId(UUID.randomUUID().toString())
                                .eventType(PaymentEvent.EventType.PAYMENT_COMPLETED)
                                .orderId(payment.getOrderId())
                                .amount(payment.getAmount())
                                .transactionId(transactionId)
                                .timestamp(LocalDateTime.now())
                                .build()
                );
                
                log.info("Payment for order {} processed successfully", payment.getOrderId());
            } else {
                // Mark payment as failed
                payment.markAsFailed();
                payment = paymentRepository.save(payment);
                
                // Publish payment failed event
                publishOutboxEvent(
                        PaymentEvent.builder()
                                .eventId(UUID.randomUUID().toString())
                                .eventType(PaymentEvent.EventType.PAYMENT_FAILED)
                                .orderId(payment.getOrderId())
                                .amount(payment.getAmount())
                                .reason("Payment processing failed")
                                .timestamp(LocalDateTime.now())
                                .build()
                );
                
                log.info("Payment for order {} failed", payment.getOrderId());
                throw new PaymentProcessingException(payment.getOrderId(), "Payment processing failed");
            }
            
            return payment;
            
        } catch (Exception e) {
            if (e instanceof PaymentProcessingException) {
                throw e;
            }
            
            log.error("Error processing payment for order {}: {}", 
                    payment.getOrderId(), e.getMessage(), e);
            
            // Mark payment as failed
            payment.markAsFailed();
            payment = paymentRepository.save(payment);
            
            // Publish payment failed event
            try {
                publishOutboxEvent(
                        PaymentEvent.builder()
                                .eventId(UUID.randomUUID().toString())
                                .eventType(PaymentEvent.EventType.PAYMENT_FAILED)
                                .orderId(payment.getOrderId())
                                .amount(payment.getAmount())
                                .reason("Error: " + e.getMessage())
                                .timestamp(LocalDateTime.now())
                                .build()
                );
            } catch (Exception ex) {
                log.error("Failed to publish payment failed event: {}", ex.getMessage(), ex);
            }
            
            throw new PaymentProcessingException(
                    payment.getOrderId(), "Payment processing error: " + e.getMessage(), e);
        }
    }
    
    private boolean simulatePaymentProcessing(Payment payment) {
        // Simulate payment processing
        // For demonstration purposes, we'll succeed 80% of the time
        log.info("Simulating payment processing for order {}", payment.getOrderId());
        
        try {
            // Simulate processing time
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // For testing, make payments below $10 fail
        return payment.getAmount().compareTo(BigDecimal.valueOf(10)) >= 0;
    }

    @Override
    @Transactional
    public Payment refundPayment(String orderId) {
        log.info("Refunding payment for order {}", orderId);
        
        Payment payment = findByOrderId(orderId);
        
        try {
            // Process refund
            payment.refund();
            payment = paymentRepository.save(payment);
            
            // Publish payment refunded event
            publishOutboxEvent(
                    PaymentEvent.builder()
                            .eventId(UUID.randomUUID().toString())
                            .eventType(PaymentEvent.EventType.PAYMENT_REFUNDED)
                            .orderId(payment.getOrderId())
                            .amount(payment.getAmount())
                            .timestamp(LocalDateTime.now())
                            .build()
            );
            
            log.info("Payment for order {} refunded successfully", orderId);
            return payment;
            
        } catch (InvalidPaymentStateException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error refunding payment for order {}: {}", orderId, e.getMessage(), e);
            throw new PaymentException("Payment refund error: " + e.getMessage(), e);
        }
    }

    @Override
    public Payment findByOrderId(String orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "orderId", orderId));
    }

    @Override
    public List<Payment> findAll() {
        return paymentRepository.findAll();
    }
    
    private void publishOutboxEvent(PaymentEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            outboxService.saveEvent(
                    "payment",
                    event.getOrderId(),
                    event.getEventType().name(),
                    payload
            );
        } catch (JsonProcessingException e) {
            log.error("Error serializing payment event: {}", e.getMessage(), e);
            throw new PaymentException("Failed to create outbox event: " + e.getMessage(), e);
        }
    }
} 