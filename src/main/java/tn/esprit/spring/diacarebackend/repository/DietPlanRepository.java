package tn.esprit.spring.diacarebackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.spring.diacarebackend.entities.DietPlan;

public interface DietPlanRepository extends JpaRepository<DietPlan, Long> {}