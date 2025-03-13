package com.example.orderservice.service;

import com.example.orderservice.dto.OrderRequest;
import com.example.orderservice.dto.OrderResponse;
import com.example.orderservice.model.OrderStatus;

import java.util.List;

/**
 * Service interface for order operations.
 */
public interface OrderService {
    
    /**
     * Create a new order
     * 
     * @param orderRequest The order request
     * @return The created order response
     */
    OrderResponse createOrder(OrderRequest orderRequest);
    
    /**
     * Get an order by ID
     * 
     * @param orderId The order ID
     * @return The order response
     */
    OrderResponse getOrder(Long orderId);
    
    /**
     * Get orders by customer ID
     * 
     * @param customerId The customer ID
     * @return List of order responses
     */
    List<OrderResponse> getOrdersByCustomer(String customerId);
    
    /**
     * Get orders by status
     * 
     * @param status The order status
     * @return List of order responses
     */
    List<OrderResponse> getOrdersByStatus(OrderStatus status);
    
    /**
     * Cancel an order
     * 
     * @param orderId The order ID
     * @return The cancelled order response
     */
    OrderResponse cancelOrder(Long orderId);
    
    /**
     * Update order status
     * 
     * @param orderId The order ID
     * @param status The new order status
     * @return The updated order response
     */
    OrderResponse updateOrderStatus(Long orderId, OrderStatus status);
} 