package tn.esprit.spring.diacarebackend.dto;

import java.util.List;   // ← java.util.List manquait !

public class DietPlanRequest {

    private Long patientId;
    private Long nutritionistId;
    private String title;
    private String description;
    private Integer targetCalories;
    private Integer targetCarbs;
    private Integer targetProtein;
    private Integer targetFat;
    private List<MealRequest> meals;  // ← manquait dans ton code !

    // ── GETTERS ──────────────────────────────────────────────

    public Long getPatientId()          { return patientId; }
    public Long getNutritionistId()     { return nutritionistId; }
    public String getTitle()            { return title; }
    public String getDescription()      { return description; }
    public Integer getTargetCalories()  { return targetCalories; }
    public Integer getTargetCarbs()     { return targetCarbs; }
    public Integer getTargetProtein()   { return targetProtein; }
    public Integer getTargetFat()       { return targetFat; }
    public List<MealRequest> getMeals() { return meals; }

    // ── SETTERS ──────────────────────────────────────────────

    public void setPatientId(Long v)               { this.patientId = v; }
    public void setNutritionistId(Long v)          { this.nutritionistId = v; }
    public void setTitle(String title)             { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setTargetCalories(Integer v)       { this.targetCalories = v; }
    public void setTargetCarbs(Integer v)          { this.targetCarbs = v; }
    public void setTargetProtein(Integer v)        { this.targetProtein = v; }
    public void setTargetFat(Integer v)            { this.targetFat = v; }
    public void setMeals(List<MealRequest> meals)  { this.meals = meals; }
}