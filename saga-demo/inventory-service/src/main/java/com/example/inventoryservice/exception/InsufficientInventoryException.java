package com.example.inventoryservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when there is not enough inventory available.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class InsufficientInventoryException extends RuntimeException {

    public InsufficientInventoryException(String productId, int requested, int available) {
        super(String.format("Insufficient inventory for product %s. Requested: %d, Available: %d", 
                productId, requested, available));
    }

    public InsufficientInventoryException(String message) {
        super(message);
    }
} 