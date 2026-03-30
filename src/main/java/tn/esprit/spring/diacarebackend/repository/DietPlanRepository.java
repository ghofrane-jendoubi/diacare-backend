package tn.esprit.spring.diacarebackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.spring.diacarebackend.entities.DietPlan;
import java.util.List;   // ← java.util.List PAS org.hibernate.mapping.List !

public interface DietPlanRepository extends JpaRepository<DietPlan, Long> {
    List<DietPlan> findByPatient_Id(Long patientId);
}