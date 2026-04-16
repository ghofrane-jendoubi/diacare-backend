package tn.esprit.spring.diacarebackend.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "food_entry")
public class FoodEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String text;

    @Column(length = 2000)
    private String analysisResult;

    @ManyToOne
    @JsonIgnore                    // ← évite boucle infinie JSON
    @JoinColumn(name = "patient_id")
    private User patient;

    // ← NOUVEAU : date création automatique
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    // ── GETTERS ──────────────────────────────────────────────
    public Long getId()                  { return id; }
    public String getText()              { return text; }
    public String getAnalysisResult()    { return analysisResult; }
    public User getPatient()             { return patient; }
    public LocalDateTime getCreatedAt()  { return createdAt; }

    // ── SETTERS ──────────────────────────────────────────────
    public void setId(Long id)                         { this.id = id; }
    public void setText(String text)                   { this.text = text; }
    public void setAnalysisResult(String r)            { this.analysisResult = r; }
    public void setPatient(User patient)               { this.patient = patient; }
    public void setCreatedAt(LocalDateTime createdAt)  { this.createdAt = createdAt; }
}