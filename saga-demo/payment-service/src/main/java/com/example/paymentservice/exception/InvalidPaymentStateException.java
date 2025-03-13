package com.example.paymentservice.exception;

import com.example.paymentservice.model.Payment;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a payment is in an invalid state for the requested operation.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class InvalidPaymentStateException extends RuntimeException {

    public InvalidPaymentStateException(String message) {
        super(message);
    }
    
    public InvalidPaymentStateException(String orderId, Payment.PaymentStatus currentStatus, String operation) {
        super(String.format("Cannot %s payment for order %s with status %s", 
                operation, orderId, currentStatus));
    }
} 