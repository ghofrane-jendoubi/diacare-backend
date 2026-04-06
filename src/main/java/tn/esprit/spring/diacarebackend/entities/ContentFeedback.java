package tn.esprit.spring.diacarebackend.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

@Entity
@Table(
        name = "content_feedback",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "content_id"})
)
public class ContentFeedback {

    private static final AtomicLong ID_SEQUENCE = new AtomicLong(System.currentTimeMillis() * 1000L);

    @Id
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long patientId;

    @Column(name = "patient_id", nullable = false)
    private Long patientIdLegacy;

    @Column(name = "content_id", nullable = false)
    private Long contentId;

    @Column(name = "educational_content_id", nullable = false)
    private Long contentIdLegacy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Emotion emotion;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = ID_SEQUENCE.incrementAndGet();
        }
        if (patientIdLegacy == null) {
            patientIdLegacy = patientId;
        }
        if (contentIdLegacy == null) {
            contentIdLegacy = contentId;
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId() { return id; }
    public Long getPatientId() { return patientId; }
    public Long getUserId() { return patientId; }
    public Long getContentId() { return contentId; }
    public Emotion getEmotion() { return emotion; }
    public String getComment() { return comment; }
    public String getOptionalComment() { return comment; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setId(Long id) { this.id = id; }
    public void setPatientId(Long patientId) {
        this.patientId = patientId;
        this.patientIdLegacy = patientId;
    }
    public void setUserId(Long userId) {
        this.patientId = userId;
        this.patientIdLegacy = userId;
    }
    public void setContentId(Long contentId) {
        this.contentId = contentId;
        this.contentIdLegacy = contentId;
    }
    public void setEmotion(Emotion emotion) { this.emotion = emotion; }
    public void setComment(String comment) { this.comment = comment; }
    public void setOptionalComment(String optionalComment) { this.comment = optionalComment; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
