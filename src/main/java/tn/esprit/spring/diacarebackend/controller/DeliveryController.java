package tn.esprit.spring.diacarebackend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.spring.diacarebackend.entities.Delivery;
import tn.esprit.spring.diacarebackend.entities.DeliveryStatus;
import tn.esprit.spring.diacarebackend.entities.Order;
import tn.esprit.spring.diacarebackend.entities.OrderStatus;
import tn.esprit.spring.diacarebackend.services.DeliveryService;
import tn.esprit.spring.diacarebackend.services.OrderService;

@RestController
@RequestMapping("/api/delivery")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;
    private final OrderService orderService;   // ← add this

    // Admin only: update delivery status
    @PutMapping("/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Delivery> updateStatus(@PathVariable Long orderId, @RequestParam String status) {
        try {
            Delivery updated = deliveryService.updateStatus(orderId, DeliveryStatus.valueOf(status));
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            // If delivery not found, create it first (if order is paid)
            Order order = orderService.getOrderById(orderId);
            if (order != null && order.getStatus() == OrderStatus.PAID) {
                deliveryService.createDelivery(order);
                Delivery updated = deliveryService.updateStatus(orderId, DeliveryStatus.valueOf(status));
                return ResponseEntity.ok(updated);
            }
            return ResponseEntity.notFound().build();
        }
    }

    // Patient & Admin: get delivery by order ID
    @GetMapping("/order/{orderId}")
    public ResponseEntity<Delivery> getDelivery(@PathVariable Long orderId) {
        try {
            return ResponseEntity.ok(deliveryService.getDeliveryForOrder(orderId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }

    }
    @GetMapping("/test")
    public String test() {
        return "Delivery controller works!";
    }
}