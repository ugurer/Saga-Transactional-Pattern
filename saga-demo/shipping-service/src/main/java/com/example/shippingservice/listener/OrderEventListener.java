package com.example.shippingservice.listener;

import com.example.shippingservice.event.OrderEvent;
import com.example.shippingservice.service.ShipmentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Kafka listener for order-related events.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {
    
    private final ShipmentService shipmentService;
    private final ObjectMapper objectMapper;
    
    @KafkaListener(topics = "order-created", groupId = "shipping-service")
    @Transactional
    public void handleOrderCreated(String payload) {
        try {
            OrderEvent event = objectMapper.readValue(payload, OrderEvent.class);
            log.info("Received ORDER_CREATED event for order: {}", event.getOrderId());
            
            // Sipariş oluşturulduğunda, sevkiyat kaydını da oluştur
            shipmentService.createShipment(
                    event.getOrderId(),
                    event.getRecipientName(),
                    event.getShippingAddress()
            );
            
        } catch (JsonProcessingException e) {
            log.error("Error deserializing order event: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error processing ORDER_CREATED event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "order-cancelled", groupId = "shipping-service")
    @Transactional
    public void handleOrderCancelled(String payload) {
        try {
            OrderEvent event = objectMapper.readValue(payload, OrderEvent.class);
            log.info("Received ORDER_CANCELLED event for order: {}", event.getOrderId());
            
            // Sipariş iptal edildiğinde, sevkiyatı da iptal et
            shipmentService.cancelShipment(
                    event.getOrderId(),
                    "Order cancelled by customer: " + (event.getReason() != null ? event.getReason() : "No reason provided")
            );
            
        } catch (JsonProcessingException e) {
            log.error("Error deserializing order event: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error processing ORDER_CANCELLED event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "payment-completed", groupId = "shipping-service")
    @Transactional
    public void handlePaymentCompleted(String payload) {
        try {
            // Ödeme tamamlandığında, sevkiyatı hazırlamaya başla
            OrderEvent event = objectMapper.readValue(payload, OrderEvent.class);
            log.info("Received PAYMENT_COMPLETED event for order: {}", event.getOrderId());
            
            shipmentService.prepareShipment(event.getOrderId());
            
            // Gerçek bir uygulamada, burada bir zamanlayıcı başlatılabilir
            // veya bir iş kuyruğuna görev eklenebilir
            // Örnek amaçlı olarak, hemen sevkiyatı gönderiyoruz
            shipmentService.shipOrder(event.getOrderId());
            
        } catch (JsonProcessingException e) {
            log.error("Error deserializing payment event: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error processing PAYMENT_COMPLETED event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "payment-failed", groupId = "shipping-service")
    @Transactional
    public void handlePaymentFailed(String payload) {
        try {
            OrderEvent event = objectMapper.readValue(payload, OrderEvent.class);
            log.info("Received PAYMENT_FAILED event for order: {}", event.getOrderId());
            
            // Ödeme başarısız olduğunda, sevkiyatı iptal et
            shipmentService.cancelShipment(
                    event.getOrderId(),
                    "Payment failed: " + (event.getReason() != null ? event.getReason() : "No reason provided")
            );
            
        } catch (JsonProcessingException e) {
            log.error("Error deserializing payment event: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error processing PAYMENT_FAILED event: {}", e.getMessage(), e);
        }
    }
} 