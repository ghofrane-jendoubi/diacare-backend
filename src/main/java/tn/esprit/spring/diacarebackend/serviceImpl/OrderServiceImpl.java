package tn.esprit.spring.diacarebackend.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.spring.diacarebackend.entities.*;
import tn.esprit.spring.diacarebackend.repository.OrderRepository;
import tn.esprit.spring.diacarebackend.services.CartService;
import tn.esprit.spring.diacarebackend.services.EmailService;
import tn.esprit.spring.diacarebackend.services.OrderService;
import tn.esprit.spring.diacarebackend.services.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final CartService cartService;
    private final OrderRepository orderRepository;
    private final UserService userService;
    private final EmailService emailService;
    // 🔥 CREATE ORDER (CHECKOUT)
    @Override
    public Order createOrder(String token) {

        Cart cart = cartService.getCart(token);
        User user = userService.getCurrentUser(token);

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        Order order = new Order();
        order.setUser(user);
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentStatus("PENDING");

        List<OrderItem> items = cart.getItems().stream().map(ci -> {
            OrderItem oi = new OrderItem();
            oi.setProduct(ci.getProduct());
            oi.setQuantity(ci.getQuantity());
            oi.setPrice(ci.getProduct().getPrice());
            oi.setOrder(order);
            return oi;
        }).toList();

        order.setItems(items);
        order.setTotalPrice(cart.getTotalPrice());



        Order savedOrder = orderRepository.save(order);

// 📧 EMAIL SIMPLE
        try {
            emailService.sendOrderEmail(
                    user.getEmail(),
                    savedOrder.getId(),
                    savedOrder.getTotalPrice()
            );
        } catch (Exception e) {
            System.out.println("EMAIL ERROR ❌");
            e.printStackTrace();
        }
        // 🧹 CLEAR CART
        cart.getItems().clear();
        cart.setTotalPrice(0);
        cartService.save(cart);

        return savedOrder;
    }

    // 🔥 GET ALL ORDERS
    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    // 🔥 CONFIRM PAYMENT
    @Override
    public Order confirmPayment(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setPaymentStatus("SUCCESS");
        order.setStatus(OrderStatus.PAID);

        return orderRepository.save(order);
    }
}