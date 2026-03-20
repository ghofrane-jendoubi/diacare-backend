package tn.esprit.spring.diacarebackend.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "patients")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Patient extends User {

    private String profilePicture;

    // Constructeur simple
    public Patient(Long id, String firstName, String lastName, String profilePicture) {
        super(id, firstName, lastName);
        this.profilePicture = profilePicture;
    }
}