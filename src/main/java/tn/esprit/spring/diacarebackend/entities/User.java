package tn.esprit.spring.diacarebackend.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String email;

    // Constructeur par défaut (obligatoire pour JPA)
    public User() {}

    // Constructeur avec paramètres (optionnel)
    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }

    // ===== GETTERS ET SETTERS =====
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}