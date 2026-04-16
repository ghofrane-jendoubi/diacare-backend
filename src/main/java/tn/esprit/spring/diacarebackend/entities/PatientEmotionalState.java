package tn.esprit.spring.diacarebackend.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "patient_emotional_state")
public class PatientEmotionalState {

    @Id
    private Long patientId;

    private Double averageScore;
    private Integer lastFeedbacksCount;
    private LocalDateTime lastUpdate;

    public PatientEmotionalState() {}

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public Double getAverageScore() { return averageScore; }
    public void setAverageScore(Double averageScore) { this.averageScore = averageScore; }
    public Integer getLastFeedbacksCount() { return lastFeedbacksCount; }
    public void setLastFeedbacksCount(Integer lastFeedbacksCount) { this.lastFeedbacksCount = lastFeedbacksCount; }
    public LocalDateTime getLastUpdate() { return lastUpdate; }
    public void setLastUpdate(LocalDateTime lastUpdate) { this.lastUpdate = lastUpdate; }
}
