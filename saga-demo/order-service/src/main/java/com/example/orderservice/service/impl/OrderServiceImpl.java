package com.example.orderservice.service.impl;

import com.example.orderservice.dto.OrderRequest;
import com.example.orderservice.dto.OrderResponse;
import com.example.orderservice.event.OrderEvent;
import com.example.orderservice.model.Order;
import com.example.orderservice.model.OrderItem;
import com.example.orderservice.model.OrderStatus;
import com.example.orderservice.repository.OrderRepository;
import com.example.orderservice.service.OrderService;
import com.example.orderservice.service.OutboxService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of the OrderService interface.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {
    
    private final OrderRepository orderRepository;
    private final OutboxService outboxService;
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public OrderResponse createOrder(OrderRequest orderRequest) {
        log.info("Creating order for customer: {}", orderRequest.getCustomerId());
        
        // Create order entity
        Order order = Order.builder()
                .customerId(orderRequest.getCustomerId())
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.ZERO) // Will be calculated when items are added
                .build();
        
        // Add order items
        orderRequest.getItems().forEach(itemRequest -> {
            OrderItem orderItem = OrderItem.builder()
                    .productId(itemRequest.getProductId())
                    .productName(itemRequest.getProductName())
                    .price(BigDecimal.valueOf(itemRequest.getPrice()))
                    .quantity(itemRequest.getQuantity())
                    .build();
            order.addOrderItem(orderItem);
        });
        
        // Save order
        Order savedOrder = orderRepository.save(order);
        log.info("Order created with ID: {}", savedOrder.getId());
        
        // Create and publish OrderCreated event via outbox
        OrderEvent orderEvent = createOrderEvent(savedOrder, OrderEvent.EventType.ORDER_CREATED);
        outboxService.saveOrderEvent(orderEvent, "Order", savedOrder.getId().toString());
        
        return mapToOrderResponse(savedOrder);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long orderId) {
        log.info("Getting order with ID: {}", orderId);
        Order order = findOrderById(orderId);
        return mapToOrderResponse(order);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByCustomer(String customerId) {
        log.info("Getting orders for customer: {}", customerId);
        List<Order> orders = orderRepository.findByCustomerId(customerId);
        return orders.stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByStatus(OrderStatus status) {
        log.info("Getting orders with status: {}", status);
        List<Order> orders = orderRepository.findByStatus(status);
        return orders.stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public OrderResponse cancelOrder(Long orderId) {
        log.info("Cancelling order with ID: {}", orderId);
        Order order = findOrderById(orderId);
        
        // Only PENDING orders can be cancelled
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Cannot cancel order with status: " + order.getStatus());
        }
        
        // Update order status
        order.setStatus(OrderStatus.CANCELLED);
        Order savedOrder = orderRepository.save(order);
        
        // Create and publish OrderCancelled event via outbox
        OrderEvent orderEvent = createOrderEvent(savedOrder, OrderEvent.EventType.ORDER_CANCELLED);
        outboxService.saveOrderEvent(orderEvent, "Order", savedOrder.getId().toString());
        
        return mapToOrderResponse(savedOrder);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus status) {
        log.info("Updating order status to {} for order ID: {}", status, orderId);
        Order order = findOrderById(orderId);
        
        // Update order status
        order.setStatus(status);
        Order savedOrder = orderRepository.save(order);
        
        // If status is COMPLETED, publish OrderCompleted event
        if (status == OrderStatus.COMPLETED) {
            OrderEvent orderEvent = createOrderEvent(savedOrder, OrderEvent.EventType.ORDER_COMPLETED);
            outboxService.saveOrderEvent(orderEvent, "Order", savedOrder.getId().toString());
        }
        
        return mapToOrderResponse(savedOrder);
    }
    
    /**
     * Find order by ID or throw EntityNotFoundException
     * 
     * @param orderId The order ID
     * @return The order entity
     * @throws EntityNotFoundException if order not found
     */
    private Order findOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with ID: " + orderId));
    }
    
    /**
     * Map Order entity to OrderResponse DTO
     * 
     * @param order The order entity
     * @return The order response DTO
     */
    private OrderResponse mapToOrderResponse(Order order) {
        List<OrderResponse.OrderItemResponse> itemResponses = order.getOrderItems().stream()
                .map(item -> OrderResponse.OrderItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .price(item.getPrice())
                        .quantity(item.getQuantity())
                        .build())
                .collect(Collectors.toList());
        
        return OrderResponse.builder()
                .id(order.getId())
                .customerId(order.getCustomerId())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .items(itemResponses)
                .build();
    }
    
    /**
     * Create OrderEvent from Order entity
     * 
     * @param order The order entity
     * @param eventType The event type
     * @return The order event
     */
    private OrderEvent createOrderEvent(Order order, String eventType) {
        List<OrderEvent.OrderItemEvent> itemEvents = order.getOrderItems().stream()
                .map(item -> OrderEvent.OrderItemEvent.builder()
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .price(item.getPrice())
                        .quantity(item.getQuantity())
                        .build())
                .collect(Collectors.toList());
        
        return OrderEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(eventType)
                .orderId(order.getId())
                .customerId(order.getCustomerId())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .items(itemEvents)
                .build();
    }
} 