package tn.esprit.spring.diacarebackend.DTOs;

public class ArticleCreateDTO {
    private String title;
    private String subtitle;
    private String content;
    private String summary;
    private String category;
    private String contentType;
    private String thumbnailUrl;
    private String videoUrl;
    private String tags;
    private Integer readingTime;
    private String difficultyLevel;
    private Boolean isFeatured;
    private Boolean isPublished;

    // GETTERS
    public String getTitle() { return title; }
    public String getSubtitle() { return subtitle; }
    public String getContent() { return content; }
    public String getSummary() { return summary; }
    public String getCategory() { return category; }
    public String getContentType() { return contentType; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public String getVideoUrl() { return videoUrl; }
    public String getTags() { return tags; }
    public Integer getReadingTime() { return readingTime; }
    public String getDifficultyLevel() { return difficultyLevel; }
    public Boolean getIsFeatured() { return isFeatured; }
    public Boolean getIsPublished() { return isPublished; }

    // SETTERS
    public void setTitle(String title) { this.title = title; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }
    public void setContent(String content) { this.content = content; }
    public void setSummary(String summary) { this.summary = summary; }
    public void setCategory(String category) { this.category = category; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
    public void setTags(String tags) { this.tags = tags; }
    public void setReadingTime(Integer readingTime) { this.readingTime = readingTime; }
    public void setDifficultyLevel(String difficultyLevel) { this.difficultyLevel = difficultyLevel; }
    public void setIsFeatured(Boolean isFeatured) { this.isFeatured = isFeatured; }
    public void setIsPublished(Boolean isPublished) { this.isPublished = isPublished; }
}