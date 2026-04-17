package tn.esprit.spring.diacarebackend.dto;

import java.time.LocalDateTime;

public class ConversationDTO {
    private Long patientId;
    private String patientName;
    private String patientProfilePicture;
    private String diabetesType;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private Long lastMessageSender;
    private Long unreadCount;
    private Boolean online;

    // ✅ Constructeur correspondant à votre appel
    public ConversationDTO(Long patientId, String patientName, String patientProfilePicture,
                           String diabetesType, String lastMessage, LocalDateTime lastMessageTime,
                           Long lastMessageSender, Long unreadCount) {
        this.patientId = patientId;
        this.patientName = patientName;
        this.patientProfilePicture = patientProfilePicture;
        this.diabetesType = diabetesType;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
        this.lastMessageSender = lastMessageSender;
        this.unreadCount = unreadCount;
        this.online = false;
    }

    // Getters et Setters
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getPatientProfilePicture() { return patientProfilePicture; }
    public void setPatientProfilePicture(String patientProfilePicture) { this.patientProfilePicture = patientProfilePicture; }

    public String getDiabetesType() { return diabetesType; }
    public void setDiabetesType(String diabetesType) { this.diabetesType = diabetesType; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public LocalDateTime getLastMessageTime() { return lastMessageTime; }
    public void setLastMessageTime(LocalDateTime lastMessageTime) { this.lastMessageTime = lastMessageTime; }

    public Long getLastMessageSender() { return lastMessageSender; }
    public void setLastMessageSender(Long lastMessageSender) { this.lastMessageSender = lastMessageSender; }

    public Long getUnreadCount() { return unreadCount; }
    public void setUnreadCount(Long unreadCount) { this.unreadCount = unreadCount; }

    public Boolean getOnline() { return online; }
    public void setOnline(Boolean online) { this.online = online; }
}