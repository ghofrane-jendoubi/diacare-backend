package tn.esprit.spring.diacarebackend.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "forum_posts")
public class ForumPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_id")
    private Long patientId;

    @Column(name = "patient_name")
    private String patientName;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    private Category category = Category.EXPERIENCE;

    @Column(name = "like_count")
    private Integer likeCount = 0;

    @Column(name = "comment_count")
    private Integer commentCount = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "is_moderated")
    private Boolean isModerated = false;

    @Column(name = "is_flagged")
    private Boolean isFlagged = false;

    @Column(name = "moderation_reason")
    private String moderationReason;

    @Column(name = "moderation_score")
    private Double moderationScore = 0.0;

    public enum Category {
        EXPERIENCE, RECETTE, ASTUCE, QUESTION, MOTIVATION
    }

    // GETTERS
    public Long getId() { return id; }
    public Long getPatientId() { return patientId; }
    public String getPatientName() { return patientName; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public Category getCategory() { return category; }
    public Integer getLikeCount() { return likeCount; }
    public Integer getCommentCount() { return commentCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Boolean getIsModerated() { return isModerated; }
    public Boolean getIsFlagged() { return isFlagged; }
    public String getModerationReason() { return moderationReason; }
    public Double getModerationScore() { return moderationScore; }

    // SETTERS
    public void setId(Long id) { this.id = id; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
    public void setCategory(Category category) { this.category = category; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }
    public void setCommentCount(Integer commentCount) { this.commentCount = commentCount; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setIsModerated(Boolean isModerated) { this.isModerated = isModerated; }
    public void setIsFlagged(Boolean isFlagged) { this.isFlagged = isFlagged; }
    public void setModerationReason(String moderationReason) { this.moderationReason = moderationReason; }
    public void setModerationScore(Double moderationScore) { this.moderationScore = moderationScore; }
}
