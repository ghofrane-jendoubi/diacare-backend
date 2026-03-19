package tn.esprit.spring.diacarebackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.spring.diacarebackend.entities.FoodEntry;

public interface FoodEntryRepository extends JpaRepository<FoodEntry, Long> {}