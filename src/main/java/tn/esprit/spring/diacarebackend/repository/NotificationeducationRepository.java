package tn.esprit.spring.diacarebackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.spring.diacarebackend.entities.Notification_education;
import java.util.List;

@Repository
public interface NotificationeducationRepository extends JpaRepository<Notification_education, Long> {

    List<Notification_education> findByDoctorIdAndIsReadFalseOrderByCreatedAtDesc(Long doctorId);

    List<Notification_education> findByDoctorIdOrderByCreatedAtDesc(Long doctorId);

    long countByDoctorIdAndIsReadFalse(Long doctorId);
}