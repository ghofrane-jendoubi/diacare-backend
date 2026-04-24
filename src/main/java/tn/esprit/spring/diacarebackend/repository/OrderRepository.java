package tn.esprit.spring.diacarebackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.spring.diacarebackend.entities.Order;
import tn.esprit.spring.diacarebackend.entities.OrderStatus;
import tn.esprit.spring.diacarebackend.entities.User;

import java.util.List;
import java.util.Optional;
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByConfirmationToken(String token);

    List<Order> findByStatus(OrderStatus status);

    List<Order> findByUser(User user);

    List<Order> findByUserId(Long userId);

}