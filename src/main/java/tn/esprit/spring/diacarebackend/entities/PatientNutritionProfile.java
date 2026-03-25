package tn.esprit.spring.diacarebackend.entities;

import jakarta.persistence.*;
import lombok.Data;

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
}