package tn.esprit.spring.diacarebackend.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "content_bookmarks",
        uniqueConstraints = @UniqueConstraint(columnNames = {"content_id", "user_id"}))
public class ContentBookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "content_id", nullable = false)
    private Long contentId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    private LocalDateTime createdAt = LocalDateTime.now();

    // ===== GETTERS =====
    public Long getId() { return id; }
    public Long getContentId() { return contentId; }
    public Long getUserId() { return userId; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // ===== SETTERS =====
    public void setId(Long id) { this.id = id; }
    public void setContentId(Long contentId) { this.contentId = contentId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
