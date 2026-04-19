package tn.esprit.spring.diacarebackend.serviceImpl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.spring.diacarebackend.entities.*;
import tn.esprit.spring.diacarebackend.repository.OrderRepository;
import tn.esprit.spring.diacarebackend.services.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    @Autowired
    private EmailService emailService;
    private final CartService cartService;
    private final OrderRepository orderRepository;
    private final UserService userService;
    private DeliveryService deliveryService;
;


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
    @Override
    public void deleteOrder(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new RuntimeException("Order not found");
        }
        orderRepository.deleteById(id);
    }
    @Override
    public Order markAsPaid(Long id, String email) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(OrderStatus.PAID);
        Order savedOrder = orderRepository.save(order);
        orderRepository.save(order);
        emailService.sendOrderConfirmationEmail(email, order.getId());

        // Determine recipient email
        String recipient = (email != null && !email.isEmpty()) ? email : order.getUser().getEmail();
        if (recipient != null && !recipient.isEmpty()) {
            emailService.sendOrderEmail(recipient, savedOrder.getId(), savedOrder.getTotalPrice());
        }

        return savedOrder;
    }

    @Override
    public Order confirmOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(OrderStatus.PAID); // Or CONFIRMED
        return orderRepository.save(order);
    }

    @Override
    public Order cancelOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Only change status
        order.setStatus(OrderStatus.CANCELLED);

        // Make sure all required fields are non-null
        if (order.getUser() == null) throw new RuntimeException("Order has no user assigned");

        return orderRepository.save(order);
    }
    @Override
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }
    // In OrderService or PaymentService
    @Override
    public void markOrderAsPaid(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(OrderStatus.PAID);  // use your enum
        orderRepository.save(order);

        // 🔥 Create delivery record
        deliveryService.createDelivery(order);
    }
    @Override
    public List<Order> getPaidOrders() {
        return orderRepository.findByStatus(OrderStatus.PAID);
    }
}