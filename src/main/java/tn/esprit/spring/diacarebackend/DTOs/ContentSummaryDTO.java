package tn.esprit.spring.diacarebackend.DTOs;

import java.time.LocalDateTime;

public class ContentSummaryDTO {
    private Long id;
    private String title;
    private String summary;
    private String category;
    private String contentType;
    private String thumbnailUrl;
    private String authorName;
    private Long viewCount;
    private Long likeCount;
    private Long commentCount;
    private Integer readingTime;
    private String difficultyLevel;
    private Boolean isFeatured;
    private String tags;
    private LocalDateTime createdAt;
    private Boolean isLiked;
    private Boolean isBookmarked;

    // ===== GETTERS =====
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getSummary() { return summary; }
    public String getCategory() { return category; }
    public String getContentType() { return contentType; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public String getAuthorName() { return authorName; }
    public Long getViewCount() { return viewCount; }
    public Long getLikeCount() { return likeCount; }
    public Long getCommentCount() { return commentCount; }
    public Integer getReadingTime() { return readingTime; }
    public String getDifficultyLevel() { return difficultyLevel; }
    public Boolean getIsFeatured() { return isFeatured; }
    public String getTags() { return tags; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Boolean getIsLiked() { return isLiked; }
    public Boolean getIsBookmarked() { return isBookmarked; }

    // ===== SETTERS =====
    public void setId(Long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setSummary(String summary) { this.summary = summary; }
    public void setCategory(String category) { this.category = category; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    public void setViewCount(Long viewCount) { this.viewCount = viewCount; }
    public void setLikeCount(Long likeCount) { this.likeCount = likeCount; }
    public void setCommentCount(Long commentCount) { this.commentCount = commentCount; }
    public void setReadingTime(Integer readingTime) { this.readingTime = readingTime; }
    public void setDifficultyLevel(String difficultyLevel) { this.difficultyLevel = difficultyLevel; }
    public void setIsFeatured(Boolean isFeatured) { this.isFeatured = isFeatured; }
    public void setTags(String tags) { this.tags = tags; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setIsLiked(Boolean isLiked) { this.isLiked = isLiked; }
    public void setIsBookmarked(Boolean isBookmarked) { this.isBookmarked = isBookmarked; }
}