package tn.esprit.spring.diacarebackend.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "doctors")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Doctor extends User {

    @Enumerated(EnumType.STRING)
    private Speciality speciality;

    private String profilePicture;

    private boolean verified; // Pour savoir si le médecin est validé par l'admin

    // Constructeur pour l'affichage dans la liste
    public Doctor(Long id, String firstName, String lastName, Speciality speciality, String profilePicture) {
        super(id, firstName, lastName);
        this.speciality = speciality;
        this.profilePicture = profilePicture;
    }
}