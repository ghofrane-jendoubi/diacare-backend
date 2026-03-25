package tn.esprit.spring.diacarebackend.dto;

public class DietPlanRequest {
    private String title;
    private String description;
    private Long patientId;
    private Long nutritionistId;

    // getters & setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public Long getNutritionistId() { return nutritionistId; }
    public void setNutritionistId(Long nutritionistId) { this.nutritionistId = nutritionistId; }
}