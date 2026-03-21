package tn.esprit.spring.diacarebackend.DTOs;

import java.time.LocalDateTime;

public class PrivateMessageDTO {
    private Long id;
    private Long senderId;
    private Long receiverId;
    private String senderName;
    private String receiverName;
    private Long contentId;
    private Long commentId;
    private String message;
    private Boolean isRead;
    private LocalDateTime createdAt;

    // GETTERS
    public Long getId() { return id; }
    public Long getSenderId() { return senderId; }
    public Long getReceiverId() { return receiverId; }
    public String getSenderName() { return senderName; }
    public String getReceiverName() { return receiverName; }
    public Long getContentId() { return contentId; }
    public Long getCommentId() { return commentId; }
    public String getMessage() { return message; }
    public Boolean getIsRead() { return isRead; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // SETTERS
    public void setId(Long id) { this.id = id; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }
    public void setReceiverId(Long receiverId) { this.receiverId = receiverId; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }
    public void setContentId(Long contentId) { this.contentId = contentId; }
    public void setCommentId(Long commentId) { this.commentId = commentId; }
    public void setMessage(String message) { this.message = message; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}