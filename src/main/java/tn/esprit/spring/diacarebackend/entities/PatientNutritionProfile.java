package tn.esprit.spring.diacarebackend.entities;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

@Entity
@Table(name = "patient_nutrition_profiles")
@Data
public class PatientNutritionProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long patientId;
    private Double weight;
    private Double height;
    private Integer age;
    private String gender;
    private String diabetesType;
    private Double hba1c;
    private String activityLevel;

    // ── Champs supplémentaires pour les besoins journaliers ──
    private Double targetCalories;
    private Double targetCarbs;
    private Double targetProtein;
    private Double targetFat;

    // Optionnel : IMC déjà calculé côté backend
    private Double bmi;

    @CreatedDate
    @Column(updatable = false)
    private java.time.LocalDateTime createdAt;

    @LastModifiedDate
    private java.time.LocalDateTime updatedAt;


}