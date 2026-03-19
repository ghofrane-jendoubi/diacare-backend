package tn.esprit.spring.diacarebackend.entities;

import jakarta.persistence.*;

@Entity
public class DietPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;

    @ManyToOne
    private User patient;

    @ManyToOne
    private User nutritionist;

    // 🔹 GETTERS

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public User getPatient() {
        return patient;
    }

    public User getNutritionist() {
        return nutritionist;
    }

    // 🔹 SETTERS

    public void setId(Long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPatient(User patient) {
        this.patient = patient;
    }

    public void setNutritionist(User nutritionist) {
        this.nutritionist = nutritionist;
    }
}