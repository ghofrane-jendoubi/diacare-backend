package tn.esprit.spring.diacarebackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.spring.diacarebackend.entities.Order;
import tn.esprit.spring.diacarebackend.entities.OrderStatus;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByConfirmationToken(String token);
    List<Order> findByStatus(OrderStatus status);
}