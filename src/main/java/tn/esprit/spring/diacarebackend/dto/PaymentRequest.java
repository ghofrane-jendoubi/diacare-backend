package tn.esprit.spring.diacarebackend.dto;

public class PaymentRequest {
    private Long appointmentId;
    private Long patientId;

    public Long getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
}