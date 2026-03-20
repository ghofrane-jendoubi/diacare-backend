package tn.esprit.spring.diacarebackend.dto;

import tn.esprit.spring.diacarebackend.entities.Doctor;
import java.time.LocalDateTime;

public class ConversationDTO {
    private Long doctorId;
    private Doctor doctor;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private int unreadCount;

    public ConversationDTO(Long doctorId, Doctor doctor, String lastMessage,
                           LocalDateTime lastMessageTime, Long unreadCount) {
        this.doctorId = doctorId;
        this.doctor = doctor;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
        this.unreadCount = unreadCount != null ? unreadCount.intValue() : 0;
    }

    // Getters et setters...
    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

    public Doctor getDoctor() { return doctor; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public LocalDateTime getLastMessageTime() { return lastMessageTime; }
    public void setLastMessageTime(LocalDateTime lastMessageTime) { this.lastMessageTime = lastMessageTime; }

    public int getUnreadCount() { return unreadCount; }
    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }
}