package tn.esprit.spring.diacarebackend.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "messagesNutrution")
public class MessageNutrition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;

    // Expéditeur : patient (id=1) ou nutritionniste (id=1 pour l'instant)
    private Long senderId;
    private String senderRole;   // "patient" ou "nutritionist"

    // Destinataire
    private Long receiverId;
    private String receiverRole;

    // Pour regrouper les messages d'une conversation
    private Long patientId;      // toujours l'id du patient de la conversation

    private boolean isRead = false;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    // ── GETTERS ──────────────────────────────────────────────

    public Long getId()             { return id; }
    public String getContent()      { return content; }
    public Long getSenderId()       { return senderId; }
    public String getSenderRole()   { return senderRole; }
    public Long getReceiverId()     { return receiverId; }
    public String getReceiverRole() { return receiverRole; }
    public Long getPatientId()      { return patientId; }
    public boolean isRead()         { return isRead; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // ── SETTERS ──────────────────────────────────────────────

    public void setId(Long id)                  { this.id = id; }
    public void setContent(String content)      { this.content = content; }
    public void setSenderId(Long senderId)      { this.senderId = senderId; }
    public void setSenderRole(String r)         { this.senderRole = r; }
    public void setReceiverId(Long receiverId)  { this.receiverId = receiverId; }
    public void setReceiverRole(String r)       { this.receiverRole = r; }
    public void setPatientId(Long patientId)    { this.patientId = patientId; }
    public void setRead(boolean read)           { this.isRead = read; }
    public void setCreatedAt(LocalDateTime t)   { this.createdAt = t; }
}