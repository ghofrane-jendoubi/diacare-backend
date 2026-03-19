package tn.esprit.spring.diacarebackend.entities;

import jakarta.persistence.*;

@Entity
public class FoodEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String text;

    @Column(length = 1000)
    private String analysisResult;

    @ManyToOne
    private User patient;

    // Getters & Setters
    public Long getId() { return id; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getAnalysisResult() { return analysisResult; }
    public void setAnalysisResult(String analysisResult) { this.analysisResult = analysisResult; }

    public User getPatient() { return patient; }
    public void setPatient(User patient) { this.patient = patient; }
}