package tn.esprit.spring.diacarebackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.spring.diacarebackend.entities.Payment;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByPaymentIntentId(String paymentIntentId);

    Optional<Payment> findByAppointmentIdAndStatus(Long appointmentId, String status);

    @Query("SELECT COUNT(p) > 0 FROM Payment p WHERE p.appointmentId = :appointmentId AND p.status = 'SUCCEEDED'")
    boolean existsByAppointmentIdAndStatus(@Param("appointmentId") Long appointmentId, @Param("status") String status);

    @Query("SELECT p FROM Payment p WHERE p.appointmentId = :appointmentId AND p.status = 'SUCCEEDED'")
    Optional<Payment> findSuccessfulPaymentByAppointmentId(@Param("appointmentId") Long appointmentId);
}