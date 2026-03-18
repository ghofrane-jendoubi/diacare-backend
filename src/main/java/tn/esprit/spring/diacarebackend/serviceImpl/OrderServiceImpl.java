package tn.esprit.spring.diacarebackend.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.spring.diacarebackend.entities.*;
import tn.esprit.spring.diacarebackend.repository.OrderRepository;
import tn.esprit.spring.diacarebackend.services.CartService;
import tn.esprit.spring.diacarebackend.services.OrderService;
import tn.esprit.spring.diacarebackend.services.UserService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final CartService cartService;
    private final OrderRepository orderRepository;
    private final UserService userService;

    @Override
    public Order createOrder(String token) {

        Cart cart = cartService.getCart(token);
        User user = userService.getCurrentUser(token);

        Order order = new Order();
        order.setUser(user);
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);

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

        return orderRepository.save(order);
    }
}