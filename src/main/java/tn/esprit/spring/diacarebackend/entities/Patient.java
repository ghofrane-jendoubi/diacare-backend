package tn.esprit.spring.diacarebackend.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "patients")
@Data
@EqualsAndHashCode(callSuper = true)
public class Patient extends User {

    @Enumerated(EnumType.STRING)
    private DiabetesType diabetesType;

    @Column(nullable = true)
    private Double weight;           // en kg

    @Column(nullable = true)
    private Double height;           // en cm

    @Enumerated(EnumType.STRING)
    private BloodType bloodType;

    private String emergencyContact; // Nom + téléphone

    @Column(length = 1000)
    private String familyHistory;    // Antécédents familiaux
}