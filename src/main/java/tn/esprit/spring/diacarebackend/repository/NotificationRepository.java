package tn.esprit.spring.diacarebackend.repository;

import tn.esprit.spring.diacarebackend.entities.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByDoctorIdAndIsReadFalseOrderByCreatedAtDesc(Long doctorId);

    List<Notification> findByDoctorIdOrderByCreatedAtDesc(Long doctorId);

    long countByDoctorIdAndIsReadFalse(Long doctorId);
}
