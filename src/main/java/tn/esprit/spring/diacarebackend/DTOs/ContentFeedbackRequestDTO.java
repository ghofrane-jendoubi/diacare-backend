package tn.esprit.spring.diacarebackend.DTOs;

public class ContentFeedbackRequestDTO {
    private Long userId;
    private String emotion;
    private String commentaire;

    public Long getUserId() { return userId; }
    public String getEmotion() { return emotion; }
    public String getCommentaire() { return commentaire; }

    public void setUserId(Long userId) { this.userId = userId; }
    public void setEmotion(String emotion) { this.emotion = emotion; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }
}
