package tn.esprit.spring.diacarebackend.dto;

import java.time.LocalDateTime;
import java.util.List;

public class CommentDTO {
    private Long id;
    private Long contentId;
    private String userName;
    private String userAvatar;
    private String commentText;
    private Long parentCommentId;
    private Integer likeCount;
    private LocalDateTime createdAt;
    private List<CommentDTO> replies;

    // ===== GETTERS =====
    public Long getId() { return id; }
    public Long getContentId() { return contentId; }
    public String getUserName() { return userName; }
    public String getUserAvatar() { return userAvatar; }
    public String getCommentText() { return commentText; }
    public Long getParentCommentId() { return parentCommentId; }
    public Integer getLikeCount() { return likeCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<CommentDTO> getReplies() { return replies; }

    // ===== SETTERS =====
    public void setId(Long id) { this.id = id; }
    public void setContentId(Long contentId) { this.contentId = contentId; }
    public void setUserName(String userName) { this.userName = userName; }
    public void setUserAvatar(String userAvatar) { this.userAvatar = userAvatar; }
    public void setCommentText(String commentText) { this.commentText = commentText; }
    public void setParentCommentId(Long parentCommentId) { this.parentCommentId = parentCommentId; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setReplies(List<CommentDTO> replies) { this.replies = replies; }
}
