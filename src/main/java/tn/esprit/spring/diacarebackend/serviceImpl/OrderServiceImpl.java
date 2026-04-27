package tn.esprit.spring.diacarebackend.serviceImpl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.esprit.spring.diacarebackend.dto.OrderDTO;
import tn.esprit.spring.diacarebackend.entities.*;
import tn.esprit.spring.diacarebackend.repository.OrderRepository;
import tn.esprit.spring.diacarebackend.repository.UserRepository;
import tn.esprit.spring.diacarebackend.services.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final EmailService emailService;
    private final CartService cartService;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final DeliveryService deliveryService;

    @Override
    @Transactional
    public Order createOrder(Long patientId) {
        log.info("Creating order for patient: {}", patientId);

        Cart cart = cartService.getCartByPatientId(patientId);

        // ✅ Force l'initialisation
        cart.getItems().size();

        User user = userRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + patientId));

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        Order order = new Order();
        order.setUser(user);
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentStatus("PENDING");

        String confirmationToken = UUID.randomUUID().toString();
        order.setConfirmationToken(confirmationToken);
        order.setTokenExpiration(LocalDateTime.now().plusHours(24));

        List<OrderItem> items = cart.getItems().stream().map(ci -> {
            OrderItem oi = new OrderItem();
            oi.setProduct(ci.getProduct());
            oi.setQuantity(ci.getQuantity());
            oi.setPrice(ci.getPrice());
            oi.setOrder(order);
            return oi;
        }).toList();

        order.setItems(items);
        order.setTotalPrice(cart.getTotalPrice());

        Order savedOrder = orderRepository.save(order);

        try {
            emailService.sendOrderEmail(
                    user.getEmail(),
                    savedOrder.getId(),
                    savedOrder.getTotalPrice()
            );
            log.info("Order confirmation email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send order email: {}", e.getMessage());
        }

        // Vider le panier
        cart.getItems().clear();
        cart.setTotalPrice(0);
        cartService.save(cart);

        return savedOrder;
    }


    // 🔥 GET ALL ORDERS
    @Override
    public List<Order> getAllOrders() {
        log.info("Fetching all orders");
        return orderRepository.findAll();
    }

    // 🔥 CONFIRM PAYMENT
    @Override
    public Order confirmPayment(Long orderId) {
        log.info("Confirming payment for order: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setPaymentStatus("SUCCESS");
        order.setStatus(OrderStatus.PAID);

        return orderRepository.save(order);
    }

    @Override
    public void deleteOrder(Long id) {
        log.info("Deleting order: {}", id);

        if (!orderRepository.existsById(id)) {
            throw new RuntimeException("Order not found");
        }
        orderRepository.deleteById(id);
    }

    @Override
    public Order markAsPaid(Long id, String email) {
        log.info("Marking order {} as paid for email: {}", id, email);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setStatus(OrderStatus.PAID);
        order.setPaymentStatus("SUCCESS");

        Order savedOrder = orderRepository.save(order);

        // Send email SYNC (wait for it to complete)
        String recipient = (email != null && !email.isEmpty()) ? email : order.getUser().getEmail();
        if (recipient != null && !recipient.isEmpty()) {
            try {
                // Call email service synchronously
                emailService.sendOrderConfirmationEmail(recipient, order.getId());
                emailService.sendOrderEmail(recipient, savedOrder.getId(), savedOrder.getTotalPrice());
                log.info("Payment confirmation email sent to: {}", recipient);

                // Small delay to ensure email is processed
                Thread.sleep(1000);
            } catch (Exception e) {
                log.error("Failed to send payment confirmation email: {}", e.getMessage());
            }
        }

        return savedOrder;
    }

    @Override
    public Order confirmOrder(Long id) {
        log.info("Confirming order: {}", id);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setStatus(OrderStatus.PAID);
        order.setPaymentStatus("SUCCESS");

        return orderRepository.save(order);
    }

    @Override
    public Order cancelOrder(Long id) {
        log.info("Cancelling order: {}", id);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setStatus(OrderStatus.CANCELLED);
        order.setPaymentStatus("CANCELLED");

        return orderRepository.save(order);
    }

    @Override
    public Order getOrderById(Long id) {
        log.info("Fetching order by id: {}", id);

        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    @Override
    @Transactional
    public void markOrderAsPaid(Long orderId) {
        log.info("Marking order as paid: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setStatus(OrderStatus.PAID);
        order.setPaymentStatus("SUCCESS");
        orderRepository.save(order);

        // 🔥 Create delivery record
        if (deliveryService != null) {
            deliveryService.createDelivery(order);
        }
    }

    @Override
    @Transactional
    public List<OrderDTO> getOrdersByPatient(Long patientId) {
        List<Order> orders = orderRepository.findByUserId(patientId);

        return orders.stream().map(order -> {
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

            dto.setItems(itemDTOs);
            return dto;
        }).toList();
    }
    @Override
    public List<Order> getPaidOrders() {
        log.info("Fetching paid orders");
        return orderRepository.findByStatus(OrderStatus.PAID);
    }
}