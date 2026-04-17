package tn.esprit.spring.diacarebackend.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "educational_content")
public class EducationalContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;
    private String subtitle;

    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String content;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Enumerated(EnumType.STRING)
    private Category category;

    @Enumerated(EnumType.STRING)
    private ContentType contentType = ContentType.ARTICLE;

    private String thumbnailUrl;
    private String videoUrl;
    private Long authorId;
    private String authorName;
    private Long viewCount = 0L;
    private Long likeCount = 0L;
    private Long commentCount = 0L;
    private Integer readingTime = 5;

    @Enumerated(EnumType.STRING)
    private DifficultyLevel difficultyLevel = DifficultyLevel.BEGINNER;

    private Boolean isFeatured = false;
    private Boolean isPublished = true;
    private String tags;
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

    // ===== ENUMS =====
    public enum Category {
        NUTRITION, EXERCISE, MEDICATION, MONITORING, LIFESTYLE, MENTAL_HEALTH
    }
    public enum ContentType {
        ARTICLE, VIDEO, INFOGRAPHIC, QUIZ
    }
    public enum DifficultyLevel {
        BEGINNER, INTERMEDIATE, ADVANCED
    }

    // ===== GETTERS =====
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getSubtitle() { return subtitle; }
    public String getContent() { return content; }
    public String getSummary() { return summary; }
    public Category getCategory() { return category; }
    public ContentType getContentType() { return contentType; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public String getVideoUrl() { return videoUrl; }
    public Long getAuthorId() { return authorId; }
    public String getAuthorName() { return authorName; }
    public Long getViewCount() { return viewCount; }
    public Long getLikeCount() { return likeCount; }
    public Long getCommentCount() { return commentCount; }
    public Integer getReadingTime() { return readingTime; }
    public DifficultyLevel getDifficultyLevel() { return difficultyLevel; }
    public Boolean getIsFeatured() { return isFeatured; }
    public Boolean getIsPublished() { return isPublished; }
    public String getTags() { return tags; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // ===== SETTERS =====
    public void setId(Long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }
    public void setContent(String content) { this.content = content; }
    public void setSummary(String summary) { this.summary = summary; }
    public void setCategory(Category category) { this.category = category; }
    public void setContentType(ContentType contentType) { this.contentType = contentType; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
    public void setAuthorId(Long authorId) { this.authorId = authorId; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    public void setViewCount(Long viewCount) { this.viewCount = viewCount; }
    public void setLikeCount(Long likeCount) { this.likeCount = likeCount; }
    public void setCommentCount(Long commentCount) { this.commentCount = commentCount; }
    public void setReadingTime(Integer readingTime) { this.readingTime = readingTime; }
    public void setDifficultyLevel(DifficultyLevel difficultyLevel) { this.difficultyLevel = difficultyLevel; }
    public void setIsFeatured(Boolean isFeatured) { this.isFeatured = isFeatured; }
    public void setIsPublished(Boolean isPublished) { this.isPublished = isPublished; }
    public void setTags(String tags) { this.tags = tags; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
