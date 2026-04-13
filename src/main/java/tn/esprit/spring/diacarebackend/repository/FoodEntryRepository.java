package tn.esprit.spring.diacarebackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.spring.diacarebackend.entities.FoodEntry;

import java.time.LocalDateTime;
import java.util.List;

public interface FoodEntryRepository extends JpaRepository<FoodEntry, Long> {

    // ✅ Utilise patient.id car c'est une relation @ManyToOne
    List<FoodEntry> findByPatientId(Long patientId);

    // ✅ Tri par date décroissante
    List<FoodEntry> findByPatientIdOrderByCreatedAtDesc(Long patientId);

    // ✅ Entre deux dates
    List<FoodEntry> findByPatientIdAndCreatedAtBetween(Long patientId, LocalDateTime start, LocalDateTime end);
}