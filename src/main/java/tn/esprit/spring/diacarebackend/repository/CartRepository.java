package tn.esprit.spring.diacarebackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.spring.diacarebackend.entities.Cart;
import tn.esprit.spring.diacarebackend.entities.User;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUser(User user);
}