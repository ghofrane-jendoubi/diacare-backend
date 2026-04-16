package tn.esprit.spring.diacarebackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.spring.diacarebackend.entities.DietMeal;
@Repository
public interface DietMealRepository extends JpaRepository<DietMeal, Long> {}