package com.example.shippingservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderEvent {

    private String eventId;
    private String orderId;
    private String customerId;
    private EventType eventType;
    private LocalDateTime timestamp;
    private BigDecimal totalAmount;
    private List<OrderItem> orderItems;
    private String recipientName;
    private String shippingAddress;
    private String reason;
    
    public enum EventType {
        ORDER_CREATED,
        ORDER_CANCELLED,
        ORDER_COMPLETED
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItem {
        private String productId;
        private Integer quantity;
        private BigDecimal price;
    }
} 