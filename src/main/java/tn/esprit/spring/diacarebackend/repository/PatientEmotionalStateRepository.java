package tn.esprit.spring.diacarebackend.repository;

import tn.esprit.spring.diacarebackend.entities.PatientEmotionalState;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PatientEmotionalStateRepository extends JpaRepository<PatientEmotionalState, Long> {
    Optional<PatientEmotionalState> findByPatientId(Long patientId);
}
