package tn.esprit.spring.diacarebackend.dto;

public class NotificationRequest {
    private Long userId;
    private String title;
    private String message;
    private String link;

    // Getters et setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }
}