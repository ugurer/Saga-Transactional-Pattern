package com.example.orderservice.event;

import com.example.orderservice.model.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Event class for order events.
 * This is used for communication between services via Kafka.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderEvent {
    
    private String eventId;
    private String eventType;
    private Long orderId;
    private String customerId;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private List<OrderItemEvent> items;
    
    /**
     * Event class for order item events.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItemEvent {
        
        private String productId;
        private String productName;
        private BigDecimal price;
        private Integer quantity;
    }
    
    /**
     * Event types for order events.
     */
    public static class EventType {
        public static final String ORDER_CREATED = "OrderCreated";
        public static final String ORDER_CANCELLED = "OrderCancelled";
        public static final String ORDER_COMPLETED = "OrderCompleted";
    }
} 