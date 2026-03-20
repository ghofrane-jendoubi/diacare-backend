package tn.esprit.spring.diacarebackend.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "content_comments")
public class ContentComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "content_id", nullable = false)
    private Long contentId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    private String userName;
    private String userAvatar;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String commentText;

    private Long parentCommentId;
    private Integer likeCount = 0;
    private Boolean isApproved = true;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ===== GETTERS =====
    public Long getId() { return id; }
    public Long getContentId() { return contentId; }
    public Long getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getUserAvatar() { return userAvatar; }
    public String getCommentText() { return commentText; }
    public Long getParentCommentId() { return parentCommentId; }
    public Integer getLikeCount() { return likeCount; }
    public Boolean getIsApproved() { return isApproved; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // ===== SETTERS =====
    public void setId(Long id) { this.id = id; }
    public void setContentId(Long contentId) { this.contentId = contentId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setUserName(String userName) { this.userName = userName; }
    public void setUserAvatar(String userAvatar) { this.userAvatar = userAvatar; }
    public void setCommentText(String commentText) { this.commentText = commentText; }
    public void setParentCommentId(Long parentCommentId) { this.parentCommentId = parentCommentId; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }
    public void setIsApproved(Boolean isApproved) { this.isApproved = isApproved; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}