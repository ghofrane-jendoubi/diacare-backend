// FoodEntryRequest.java
package tn.esprit.spring.diacarebackend.dto;

public class FoodEntryRequest {
    private String text;
    private Long patientId;

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
}