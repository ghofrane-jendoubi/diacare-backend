package tn.esprit.spring.diacarebackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.spring.diacarebackend.entities.User;
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Les méthodes findAll(), save(), etc. sont automatiquement disponibles
}