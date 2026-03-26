package tn.esprit.spring.diacarebackend.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.spring.diacarebackend.user.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findByActivationToken(String activationToken);
}