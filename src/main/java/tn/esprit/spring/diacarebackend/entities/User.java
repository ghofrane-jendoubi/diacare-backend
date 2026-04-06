package tn.esprit.spring.diacarebackend.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String prenom;
    private String email;
    private String password;
    private String role;

    @Column(name = "full_name")
    private String fullName; // on le laisse, mais on le remplit via un trigger ou on le calcule

    @Column(name = "doctor_id")
    private Long doctorId;

    // Getters et setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getFullName() {
        if (fullName != null) return fullName;
        if (nom != null && prenom != null) return nom + " " + prenom;
        return email;
    }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }
}
