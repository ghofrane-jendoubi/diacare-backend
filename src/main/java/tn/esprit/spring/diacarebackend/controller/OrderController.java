package tn.esprit.spring.diacarebackend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.spring.diacarebackend.entities.Order;
import tn.esprit.spring.diacarebackend.entities.OrderStatus;
import tn.esprit.spring.diacarebackend.repository.OrderRepository;
import tn.esprit.spring.diacarebackend.services.OrderService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderRepository orderRepository;

    @PostMapping("/checkout")
    public Order checkout(
            @RequestHeader(value = "Authorization", required = false) String token
    ) {
        return orderService.createOrder(token);
    }

    @GetMapping
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    @PostMapping("/pay/{id}")
    public Order pay(@PathVariable Long id) {
        return orderService.confirmPayment(id);
    }

    @GetMapping("/confirm")
    public String confirm(@RequestParam String token, @RequestParam String choice) {

        Order order = orderRepository.findByConfirmationToken(token)
                .orElseThrow(() -> new RuntimeException("Token invalide"));

        // ⏰ expiration
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
}