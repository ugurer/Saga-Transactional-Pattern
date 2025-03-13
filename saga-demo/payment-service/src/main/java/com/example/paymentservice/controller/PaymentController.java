package com.example.paymentservice.controller;

import com.example.paymentservice.model.Payment;
import com.example.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * REST controller for payment operations.
 */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {
    
    private final PaymentService paymentService;
    
    /**
     * Get all payments.
     * 
     * @return List of payments
     */
    @GetMapping
    public ResponseEntity<List<Payment>> getAllPayments() {
        return ResponseEntity.ok(paymentService.findAll());
    }
    
    /**
     * Get payment by ID.
     * 
     * @param id The payment ID
     * @return The payment if found, or 404 Not Found
     */
    @GetMapping("/{id}")
    public ResponseEntity<Payment> getPaymentById(@PathVariable Long id) {
        return paymentService.getPaymentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get payment by order ID.
     * 
     * @param orderId The order ID
     * @return The payment if found, or 404 Not Found
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<Payment> getPaymentByOrderId(@PathVariable String orderId) {
        return ResponseEntity.ok(paymentService.findByOrderId(orderId));
    }
    
    /**
     * Process a payment.
     * 
     * @param request The payment request
     * @return The processed payment
     */
    @PostMapping("/process")
    public ResponseEntity<Payment> processPayment(
            @RequestBody Map<String, Object> request) {
        String orderId = (String) request.get("orderId");
        BigDecimal amount = new BigDecimal(request.get("amount").toString());
        String paymentMethod = request.getOrDefault("paymentMethod", "CREDIT_CARD").toString();

        log.info("Received request to process payment for order {}, amount {}, method {}",
                orderId, amount, paymentMethod);

        Payment payment = paymentService.processPayment(orderId, amount, paymentMethod);
        return ResponseEntity.status(HttpStatus.CREATED).body(payment);
    }
    
    /**
     * Refund a payment.
     * 
     * @param orderId The order ID
     * @return The refunded payment
     */
    @PostMapping("/refund/{orderId}")
    public ResponseEntity<Payment> refundPayment(@PathVariable String orderId) {
        log.info("Received request to refund payment for order {}", orderId);
        return ResponseEntity.ok(paymentService.refundPayment(orderId));
    }
} 