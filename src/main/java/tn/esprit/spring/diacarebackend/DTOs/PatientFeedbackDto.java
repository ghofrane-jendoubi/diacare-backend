package tn.esprit.spring.diacarebackend.DTOs;

import tn.esprit.spring.diacarebackend.entities.Emotion;

import java.time.LocalDateTime;

public class PatientFeedbackDto {
    private Long patientId;
    private String patientName;
    private Long contentId;
    private String contentTitle;
    private Emotion emotion;
    private String comment;
    private LocalDateTime createdAt;

    public PatientFeedbackDto() {
    }

    public PatientFeedbackDto(String patientName, String contentTitle, Emotion emotion, String comment, LocalDateTime createdAt) {
        this.patientName = patientName;
        this.contentTitle = contentTitle;
        this.emotion = emotion;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    public PatientFeedbackDto(Long patientId, String patientName, Long contentId, String contentTitle, Emotion emotion, String comment, LocalDateTime createdAt) {
        this.patientId = patientId;
        this.patientName = patientName;
        this.contentId = contentId;
        this.contentTitle = contentTitle;
        this.emotion = emotion;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    public Long getPatientId() { return patientId; }
    public String getPatientName() { return patientName; }
    public Long getContentId() { return contentId; }
    public String getContentTitle() { return contentTitle; }
    public Emotion getEmotion() { return emotion; }
    public String getComment() { return comment; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public void setContentId(Long contentId) { this.contentId = contentId; }
    public void setContentTitle(String contentTitle) { this.contentTitle = contentTitle; }
    public void setEmotion(Emotion emotion) { this.emotion = emotion; }
    public void setComment(String comment) { this.comment = comment; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
