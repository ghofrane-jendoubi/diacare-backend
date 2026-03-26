package tn.esprit.spring.diacarebackend.repository;

import tn.esprit.spring.diacarebackend.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}