package com.example.orderservice.repository;

import com.example.orderservice.model.Order;
import com.example.orderservice.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Order entity operations.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    /**
     * Find orders by customer ID
     * 
     * @param customerId The customer ID
     * @return List of orders for the customer
     */
    List<Order> findByCustomerId(String customerId);
    
    /**
     * Find orders by status
     * 
     * @param status The order status
     * @return List of orders with the given status
     */
    List<Order> findByStatus(OrderStatus status);
    
    /**
     * Find orders by customer ID and status
     * 
     * @param customerId The customer ID
     * @param status The order status
     * @return List of orders for the customer with the given status
     */
    List<Order> findByCustomerIdAndStatus(String customerId, OrderStatus status);
} 