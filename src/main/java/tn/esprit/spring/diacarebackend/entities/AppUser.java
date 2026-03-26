package tn.esprit.spring.diacarebackend.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "app_users")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.PATIENT;

    @Column(name = "avatar_letter")
    private String avatarLetter;

    @Column(name = "diabetes_type")
    private String diabetesType;

    private String specialty;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Role { PATIENT, DOCTOR }

    // GETTERS
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public Role getRole() { return role; }
    public String getAvatarLetter() { return avatarLetter; }
    public String getDiabetesType() { return diabetesType; }
    public String getSpecialty() { return specialty; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // SETTERS
    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setRole(Role role) { this.role = role; }
    public void setAvatarLetter(String avatarLetter) { this.avatarLetter = avatarLetter; }
    public void setDiabetesType(String diabetesType) { this.diabetesType = diabetesType; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}