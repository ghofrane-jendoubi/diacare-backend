package tn.esprit.spring.diacarebackend.DTOs;

import tn.esprit.spring.diacarebackend.entities.Emotion;

public class FeedbackRequest {
    private Emotion emotion;
    private String comment;

    public Emotion getEmotion() { return emotion; }
    public String getComment() { return comment; }

    public void setEmotion(Emotion emotion) { this.emotion = emotion; }
    public void setComment(String comment) { this.comment = comment; }
}
