package com.example.shippingservice.service;

import com.example.shippingservice.model.Shipment;

import java.util.List;

/**
 * Service interface for managing shipments.
 */
public interface ShipmentService {
    
    /**
     * Create a new shipment.
     *
     * @param orderId The order ID
     * @param recipientName The recipient name
     * @param shippingAddress The shipping address
     * @return The created shipment
     */
    Shipment createShipment(String orderId, String recipientName, String shippingAddress);
    
    /**
     * Prepare a shipment for shipping.
     *
     * @param orderId The order ID
     * @return The updated shipment
     */
    Shipment prepareShipment(String orderId);
    
    /**
     * Ship an order.
     *
     * @param orderId The order ID
     * @return The updated shipment
     */
    Shipment shipOrder(String orderId);
    
    /**
     * Mark an order as delivered.
     *
     * @param orderId The order ID
     * @return The updated shipment
     */
    Shipment deliverOrder(String orderId);
    
    /**
     * Cancel a shipment.
     *
     * @param orderId The order ID
     * @param reason The reason for cancellation
     * @return The updated shipment
     */
    Shipment cancelShipment(String orderId, String reason);
    
    /**
     * Get a shipment by order ID.
     *
     * @param orderId The order ID
     * @return The shipment
     * @throws IllegalArgumentException if no shipment found for the given order ID
     */
    Shipment getShipmentByOrderId(String orderId);
    
    /**
     * Get all shipments.
     *
     * @return A list of all shipments
     */
    List<Shipment> findAll();
} 