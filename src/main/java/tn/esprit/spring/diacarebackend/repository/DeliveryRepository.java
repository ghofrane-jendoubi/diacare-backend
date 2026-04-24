package tn.esprit.spring.diacarebackend.repository;


import org.springframework.stereotype.Repository;
import tn.esprit.spring.diacarebackend.entities.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    Optional<Delivery> findByOrderId(Long orderId);
}