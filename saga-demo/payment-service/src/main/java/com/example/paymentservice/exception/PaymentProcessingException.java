package com.example.paymentservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when payment processing fails.
 */
@ResponseStatus(HttpStatus.PAYMENT_REQUIRED)
public class PaymentProcessingException extends RuntimeException {

    public PaymentProcessingException(String message) {
        super(message);
    }

    public PaymentProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public PaymentProcessingException(String orderId, String reason) {
        super(String.format("Payment processing failed for order %s: %s", orderId, reason));
    }
} 