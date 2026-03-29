package tn.esprit.spring.diacarebackend.dto;

import java.time.LocalDateTime;

public class DoctorConversationDTO {
    private Long doctorId;
    private String doctorName;
    private String doctorProfilePicture;
    private String speciality;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private Long lastMessageSender;
    private Long unreadCount;


    public DoctorConversationDTO(Long doctorId, String doctorName, String doctorProfilePicture,
                                 String speciality, String lastMessage, LocalDateTime lastMessageTime,
                                 Long lastMessageSender, Long unreadCount) {
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.doctorProfilePicture = doctorProfilePicture;
        this.speciality = speciality;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
        this.lastMessageSender = lastMessageSender;
        this.unreadCount = unreadCount;
    }

    // Getters et setters
    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }
    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
    public String getDoctorProfilePicture() { return doctorProfilePicture; }
    public void setDoctorProfilePicture(String doctorProfilePicture) { this.doctorProfilePicture = doctorProfilePicture; }
    public String getSpeciality() { return speciality; }
    public void setSpeciality(String speciality) { this.speciality = speciality; }
    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }
    public LocalDateTime getLastMessageTime() { return lastMessageTime; }
    public void setLastMessageTime(LocalDateTime lastMessageTime) { this.lastMessageTime = lastMessageTime; }
    public Long getLastMessageSender() { return lastMessageSender; }
    public void setLastMessageSender(Long lastMessageSender) { this.lastMessageSender = lastMessageSender; }
    public Long getUnreadCount() { return unreadCount; }
    public void setUnreadCount(Long unreadCount) { this.unreadCount = unreadCount; }

}