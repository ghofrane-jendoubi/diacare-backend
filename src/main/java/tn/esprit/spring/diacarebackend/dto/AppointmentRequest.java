package tn.esprit.spring.diacarebackend.dto;

import java.time.LocalDateTime;

public class AppointmentRequest {
    private Long doctorId;
    private Long patientId;
    private String patientName;
    private String title;
    private LocalDateTime start;
    private LocalDateTime end;
    private String description;
    private String type;
    private String meetLink;
    private String status;
    private Double fee;
    private Boolean paid;

    // Getters et setters
    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public LocalDateTime getStart() { return start; }
    public void setStart(LocalDateTime start) { this.start = start; }

    public LocalDateTime getEnd() { return end; }
    public void setEnd(LocalDateTime end) { this.end = end; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getMeetLink() { return meetLink; }
    public void setMeetLink(String meetLink) { this.meetLink = meetLink; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Double getFee() { return fee; }
    public void setFee(Double fee) { this.fee = fee; }

    public Boolean getPaid() { return paid; }
    public void setPaid(Boolean paid) { this.paid = paid; }
}