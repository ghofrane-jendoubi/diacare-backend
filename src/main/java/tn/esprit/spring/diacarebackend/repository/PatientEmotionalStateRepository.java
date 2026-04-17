package tn.esprit.spring.diacarebackend.repository;

import org.springframework.stereotype.Repository;
import tn.esprit.spring.diacarebackend.entities.PatientEmotionalState;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
@Repository
public interface PatientEmotionalStateRepository extends JpaRepository<PatientEmotionalState, Long> {
    Optional<PatientEmotionalState> findByPatientId(Long patientId);
}
