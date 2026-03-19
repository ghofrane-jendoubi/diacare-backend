package tn.esprit.spring.diacarebackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.spring.diacarebackend.entities.DietMeal;

public interface DietMealRepository extends JpaRepository<DietMeal, Long> {}