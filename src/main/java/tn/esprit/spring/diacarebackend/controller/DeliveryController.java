package tn.esprit.spring.diacarebackend.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.spring.diacarebackend.entities.Delivery;
import tn.esprit.spring.diacarebackend.entities.DeliveryStatus;
import tn.esprit.spring.diacarebackend.entities.Order;
import tn.esprit.spring.diacarebackend.entities.OrderStatus;
import tn.esprit.spring.diacarebackend.services.DeliveryService;
import tn.esprit.spring.diacarebackend.services.OrderService;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/delivery")
public class DeliveryController {

    private final DeliveryService deliveryService;
    private final OrderService orderService;

    public DeliveryController(DeliveryService deliveryService,
                              @Lazy OrderService orderService) {
        this.deliveryService = deliveryService;
        this.orderService = orderService;
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long orderId,
            @RequestParam String status) {
        try {
            DeliveryStatus deliveryStatus = DeliveryStatus.valueOf(status.toUpperCase());
            Delivery updated = deliveryService.updateStatus(orderId, deliveryStatus);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Statut invalide: " + status));
        } catch (Exception e) {
            log.error("Error updating delivery for order {}: {}", orderId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<?> getDelivery(@PathVariable Long orderId) {
        try {
            Delivery delivery = deliveryService.getDeliveryForOrder(orderId);
            return ResponseEntity.ok(delivery);
        } catch (RuntimeException e) {
            // Pas encore de livraison = normal, retourner 404 proprement
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/test")
    public String test() {
        return "Delivery controller works!";
    }
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Delivery>> getAllDeliveries() {
        try {
            List<Delivery> deliveries = deliveryService.getAllDeliveries();
            return ResponseEntity.ok(deliveries);
        } catch (Exception e) {
            log.error("Error fetching all deliveries: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}