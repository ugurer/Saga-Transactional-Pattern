package com.example.shippingservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * General exception for shipment-related errors.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ShipmentException extends RuntimeException {

    public ShipmentException(String message) {
        super(message);
    }

    public ShipmentException(String message, Throwable cause) {
        super(message, cause);
    }
} 