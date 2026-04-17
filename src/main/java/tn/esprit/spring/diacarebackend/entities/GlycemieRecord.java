package tn.esprit.spring.diacarebackend.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "glycemie_records")
public class GlycemieRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(nullable = false)
    private Double valeur;

    @Enumerated(EnumType.STRING)
    private Moment moment;

    private String notes;

    private LocalDateTime measuredAt = LocalDateTime.now();

    public enum Moment {
        AVANT_REPAS, APRES_REPAS, A_JEUN, AVANT_DODO, AUTRE
    }

    // GETTERS
    public Long getId() { return id; }
    public Long getPatientId() { return patientId; }
    public Double getValeur() { return valeur; }
    public Moment getMoment() { return moment; }
    public String getNotes() { return notes; }
    public LocalDateTime getMeasuredAt() { return measuredAt; }

    // SETTERS
    public void setId(Long id) { this.id = id; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public void setValeur(Double valeur) { this.valeur = valeur; }
    public void setMoment(Moment moment) { this.moment = moment; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setMeasuredAt(LocalDateTime measuredAt) { this.measuredAt = measuredAt; }
}
