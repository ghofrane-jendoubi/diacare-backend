package tn.esprit.spring.diacarebackend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import tn.esprit.spring.diacarebackend.dto.OrderDTO;
import tn.esprit.spring.diacarebackend.entities.Order;
import tn.esprit.spring.diacarebackend.entities.OrderStatus;
import tn.esprit.spring.diacarebackend.entities.User;
import tn.esprit.spring.diacarebackend.repository.OrderRepository;
import tn.esprit.spring.diacarebackend.repository.UserRepository;
import tn.esprit.spring.diacarebackend.services.OrderService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    // ✅ Endpoint de test
    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> test() {
        return ResponseEntity.ok(Map.of("status", "OK", "message", "Order API is working!"));
    }

    @GetMapping
    @Transactional
    public ResponseEntity<?> getAllOrders(@RequestParam(required = false) Long patientId) {
        try {
            if (patientId != null && patientId > 0) {
                List<OrderDTO> orders = orderService.getOrdersByPatient(patientId);
                return ResponseEntity.ok(orders);
            } else {
                // Pour l'admin - tu peux faire pareil avec un DTO
                return ResponseEntity.ok(orderRepository.findAll());
            }
        } catch (Exception e) {
            log.error("Error fetching orders: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    @PostMapping("/checkout")
    @Transactional
    public ResponseEntity<?> checkout(@RequestParam Long patientId) {
        try {
            log.info("Checkout for patient: {}", patientId);
            Order order = orderService.createOrder(patientId);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            log.error("Checkout error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ✅ Payer une commande
    @PostMapping("/pay/{id}")
    public ResponseEntity<?> pay(@PathVariable Long id) {
        try {
            Order order = orderService.confirmPayment(id);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            log.error("Payment error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ✅ Confirmation par token
    @GetMapping("/confirm")
    public String confirm(@RequestParam String token, @RequestParam String choice) {
        Order order = orderRepository.findByConfirmationToken(token)
                .orElseThrow(() -> new RuntimeException("Token invalide"));

        if (order.getTokenExpiration().isBefore(LocalDateTime.now())) {
            return "Lien expiré ❌";
        }

        if (choice.equals("yes")) {
            order.setStatus(OrderStatus.PAID);
            order.setPaymentStatus("SUCCESS");
        } else {
            order.setStatus(OrderStatus.PENDING);
            order.setPaymentStatus("FAILED");
        }

        orderRepository.save(order);
        return "Confirmation OK ✔";
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<Order> markOrderAsPaid(@PathVariable Long id,
                                                 @RequestParam(required = false) String email) {
        Order order = orderService.markAsPaid(id, email);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        Order order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<Order> confirmOrder(@PathVariable Long id) {
        Order order = orderService.confirmOrder(id);
        return ResponseEntity.ok(order);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Order> cancelOrder(@PathVariable Long id) {
        Order order = orderService.cancelOrder(id);
        return ResponseEntity.ok(order);
    }
    @GetMapping("/admin/paid-orders")
    @Transactional
    public ResponseEntity<?> getPaidOrders() {
        try {
            List<Order> orders = orderService.getPaidOrders();

            List<OrderDTO> dtos = orders.stream().map(order -> {
                OrderDTO dto = new OrderDTO();
                dto.setId(order.getId());
                dto.setCreatedAt(order.getCreatedAt());
                dto.setStatus(order.getStatus() != null ? order.getStatus().name() : null);
                dto.setPaymentStatus(order.getPaymentStatus());
                dto.setTotalPrice(order.getTotalPrice());

                List<OrderDTO.OrderItemDTO> itemDTOs = order.getItems().stream().map(item -> {
                    OrderDTO.OrderItemDTO itemDTO = new OrderDTO.OrderItemDTO();
                    itemDTO.setId(item.getId());
                    itemDTO.setQuantity(item.getQuantity());
                    itemDTO.setPrice(item.getPrice());

                    if (item.getProduct() != null) {
                        OrderDTO.ProductDTO productDTO = new OrderDTO.ProductDTO();
                        productDTO.setId(item.getProduct().getId());
                        productDTO.setName(item.getProduct().getName());
                        productDTO.setPrice(item.getProduct().getPrice());
                        productDTO.setImage(item.getProduct().getImage());
                        itemDTO.setProduct(productDTO);
                    }
                    return itemDTO;
                }).toList();

                // Infos patient pour l'admin
                if (order.getUser() != null) {
                    dto.setPatientFirstName(order.getUser().getFirstName());
                    dto.setPatientLastName(order.getUser().getLastName());
                    dto.setPatientEmail(order.getUser().getEmail());
                }

                dto.setItems(itemDTOs);
                return dto;
            }).toList();

            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            log.error("Error fetching paid orders: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}