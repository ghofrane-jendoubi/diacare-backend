package tn.esprit.spring.diacarebackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.spring.diacarebackend.entities.PatientNutritionProfile;
import java.util.Optional;
@Repository
public interface PatientNutritionProfileRepository
        extends JpaRepository<PatientNutritionProfile, Long> {

    Optional<PatientNutritionProfile> findByPatientId(Long patientId);
}