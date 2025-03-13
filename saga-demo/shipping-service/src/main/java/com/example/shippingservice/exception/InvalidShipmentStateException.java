package com.example.shippingservice.exception;

import com.example.shippingservice.model.Shipment;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a shipment is in an invalid state for the requested operation.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class InvalidShipmentStateException extends RuntimeException {

    public InvalidShipmentStateException(String message) {
        super(message);
    }
    
    public InvalidShipmentStateException(String orderId, Shipment.ShipmentStatus currentStatus, String operation) {
        super(String.format("Cannot %s shipment for order %s with status %s", 
                operation, orderId, currentStatus));
    }
} 