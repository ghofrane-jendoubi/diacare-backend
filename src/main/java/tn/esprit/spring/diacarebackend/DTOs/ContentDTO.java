package tn.esprit.spring.diacarebackend.DTOs;

import java.time.LocalDateTime;
import java.util.List;

public class ContentDTO {

    private Long id;
    private String title;
    private String subtitle;
    private String content;
    private String summary;
    private String category;
    private String contentType;
    private String thumbnailUrl;
    private String videoUrl;
    private Long authorId;
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
    private Boolean isPublished;
    private List<CommentDTO> comments;

    // ===== GETTERS =====
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getSubtitle() { return subtitle; }
    public String getContent() { return content; }
    public String getSummary() { return summary; }
    public String getCategory() { return category; }
    public String getContentType() { return contentType; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public String getVideoUrl() { return videoUrl; }
    public Long getAuthorId() { return authorId; }
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
    public Boolean getIsPublished() { return isPublished; }
    public List<CommentDTO> getComments() { return comments; }

    // ===== SETTERS =====
    public void setId(Long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }
    public void setContent(String content) { this.content = content; }
    public void setSummary(String summary) { this.summary = summary; }
    public void setCategory(String category) { this.category = category; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
    public void setAuthorId(Long authorId) { this.authorId = authorId; }
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
    public void setIsPublished(Boolean isPublished) { this.isPublished = isPublished; }
    public void setComments(List<CommentDTO> comments) { this.comments = comments; }
}