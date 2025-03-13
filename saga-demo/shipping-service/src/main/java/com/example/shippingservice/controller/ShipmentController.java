package com.example.shippingservice.controller;

import com.example.shippingservice.model.Shipment;
import com.example.shippingservice.service.ShipmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for shipment operations.
 */
@RestController
@RequestMapping("/api/shipments")
@RequiredArgsConstructor
@Slf4j
public class ShipmentController {
    
    private final ShipmentService shipmentService;
    
    /**
     * Get all shipments.
     * 
     * @return List of shipments
     */
    @GetMapping
    public ResponseEntity<List<Shipment>> getAllShipments() {
        return ResponseEntity.ok(shipmentService.getAllShipments());
    }
    
    /**
     * Get shipment by ID.
     * 
     * @param id The shipment ID
     * @return The shipment if found, or 404 Not Found
     */
    @GetMapping("/{id}")
    public ResponseEntity<Shipment> getShipmentById(@PathVariable Long id) {
        return shipmentService.getShipmentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get shipment by order ID.
     * 
     * @param orderId The order ID
     * @return The shipment if found, or 404 Not Found
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<Shipment> getShipmentByOrderId(@PathVariable String orderId) {
        log.info("Sevkiyat bilgisi isteniyor, sipariş ID: {}", orderId);
        try {
            Shipment shipment = shipmentService.getShipmentByOrderId(orderId);
            return ResponseEntity.ok(shipment);
        } catch (IllegalArgumentException e) {
            log.warn("Sevkiyat bulunamadı, sipariş ID: {}", orderId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Sevkiyat bilgisi alınırken hata oluştu: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Create a new shipment.
     * 
     * @param request The shipment request
     * @return The created shipment
     */
    @PostMapping
    public ResponseEntity<Shipment> createShipment(@RequestBody Map<String, String> request) {
        try {
            String orderId = request.get("orderId");
            String recipientName = request.get("recipientName");
            String shippingAddress = request.get("shippingAddress");
            
            if (orderId == null || recipientName == null || shippingAddress == null) {
                return ResponseEntity.badRequest().build();
            }
            
            log.info("Yeni sevkiyat oluşturuluyor, sipariş ID: {}", orderId);
            Shipment shipment = shipmentService.createShipment(orderId, recipientName, shippingAddress);
            return ResponseEntity.status(HttpStatus.CREATED).body(shipment);
        } catch (Exception e) {
            log.error("Sevkiyat oluşturulurken hata oluştu: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Update shipment status.
     * 
     * @param orderId The order ID
     * @param statusRequest The status update request
     * @return The updated shipment
     */
    @PutMapping("/status/{orderId}")
    public ResponseEntity<Shipment> updateShipmentStatus(
            @PathVariable String orderId,
            @RequestBody Map<String, String> statusRequest) {
        try {
            String statusStr = statusRequest.get("status");
            if (statusStr == null) {
                return ResponseEntity.badRequest().build();
            }
            
            Shipment.ShipmentStatus status = Shipment.ShipmentStatus.valueOf(statusStr.toUpperCase());
            Shipment shipment = shipmentService.updateShipmentStatus(orderId, status);
            return ResponseEntity.ok(shipment);
        } catch (IllegalArgumentException e) {
            log.error("Shipment not found or invalid status: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error updating shipment status: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Cancel a shipment.
     * 
     * @param orderId The order ID
     * @param request The cancellation request
     * @return The cancelled shipment
     */
    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<Shipment> cancelShipment(
            @PathVariable String orderId,
            @RequestBody(required = false) Map<String, String> request) {
        
        String reason = request != null ? request.get("reason") : "Müşteri tarafından iptal edildi";
        
        try {
            log.info("Sevkiyat iptal ediliyor, sipariş ID: {}, sebep: {}", orderId, reason);
            Shipment shipment = shipmentService.cancelShipment(orderId, reason);
            return ResponseEntity.ok(shipment);
        } catch (IllegalArgumentException e) {
            log.warn("Sevkiyat bulunamadı, sipariş ID: {}", orderId);
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            log.warn("Geçersiz sevkiyat durumu, sipariş ID: {}, hata: {}", orderId, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Sevkiyat iptal edilirken hata oluştu: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Ship an order.
     * 
     * @param orderId The order ID
     * @return The shipped shipment
     */
    @PutMapping("/{orderId}/ship")
    public ResponseEntity<Shipment> shipOrder(@PathVariable String orderId) {
        try {
            log.info("Sipariş gönderiliyor, sipariş ID: {}", orderId);
            Shipment shipment = shipmentService.shipOrder(orderId);
            return ResponseEntity.ok(shipment);
        } catch (IllegalArgumentException e) {
            log.warn("Sevkiyat bulunamadı, sipariş ID: {}", orderId);
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            log.warn("Geçersiz sevkiyat durumu, sipariş ID: {}, hata: {}", orderId, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Sipariş gönderilirken hata oluştu: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Mark an order as delivered.
     * 
     * @param orderId The order ID
     * @return The delivered shipment
     */
    @PutMapping("/{orderId}/deliver")
    public ResponseEntity<Shipment> deliverOrder(@PathVariable String orderId) {
        try {
            log.info("Sipariş teslim ediliyor, sipariş ID: {}", orderId);
            Shipment shipment = shipmentService.deliverOrder(orderId);
            return ResponseEntity.ok(shipment);
        } catch (IllegalArgumentException e) {
            log.warn("Sevkiyat bulunamadı, sipariş ID: {}", orderId);
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            log.warn("Geçersiz sevkiyat durumu, sipariş ID: {}, hata: {}", orderId, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Sipariş teslim edilirken hata oluştu: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{orderId}/prepare")
    public ResponseEntity<Shipment> prepareShipment(@PathVariable String orderId) {
        try {
            log.info("Sevkiyat hazırlanıyor, sipariş ID: {}", orderId);
            Shipment shipment = shipmentService.prepareShipment(orderId);
            return ResponseEntity.ok(shipment);
        } catch (IllegalArgumentException e) {
            log.warn("Sevkiyat bulunamadı, sipariş ID: {}", orderId);
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            log.warn("Geçersiz sevkiyat durumu, sipariş ID: {}, hata: {}", orderId, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Sevkiyat hazırlanırken hata oluştu: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{orderId}/status")
    public ResponseEntity<Map<String, Object>> getShipmentStatus(@PathVariable String orderId) {
        try {
            log.info("Sevkiyat durumu isteniyor, sipariş ID: {}", orderId);
            Shipment shipment = shipmentService.getShipmentByOrderId(orderId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("orderId", shipment.getOrderId());
            response.put("status", shipment.getStatus().name());
            response.put("trackingNumber", shipment.getTrackingNumber());
            response.put("updatedAt", shipment.getUpdatedAt());
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Sevkiyat bulunamadı, sipariş ID: {}", orderId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Sevkiyat durumu alınırken hata oluştu: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
} 