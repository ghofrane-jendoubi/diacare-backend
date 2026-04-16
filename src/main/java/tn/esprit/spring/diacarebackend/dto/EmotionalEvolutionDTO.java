package tn.esprit.spring.diacarebackend.dto;

import java.time.LocalDateTime;

public class EmotionalEvolutionDTO {
    private LocalDateTime date;
    private Double score;
    private Long contentId;
    private String emotion;

    public EmotionalEvolutionDTO() {}

    public EmotionalEvolutionDTO(LocalDateTime date, Double score, Long contentId, String emotion) {
        this.date = date;
        this.score = score;
        this.contentId = contentId;
        this.emotion = emotion;
    }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }
    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }
    public Long getContentId() { return contentId; }
    public void setContentId(Long contentId) { this.contentId = contentId; }
    public String getEmotion() { return emotion; }
    public void setEmotion(String emotion) { this.emotion = emotion; }
}
