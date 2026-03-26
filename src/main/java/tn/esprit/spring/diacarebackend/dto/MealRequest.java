package tn.esprit.spring.diacarebackend.dto;

// ← Classe manquante dans ton code ! DietPlanService utilise MealRequest
//   mais elle n'existe pas dans le package dto

public class MealRequest {

    private String mealType;     // breakfast, lunch, dinner, snack
    private String food;
    private Integer targetCarbs;
    private String notes;

    // ── GETTERS ──────────────────────────────────────────────

    public String getMealType()     { return mealType; }
    public String getFood()         { return food; }
    public Integer getTargetCarbs() { return targetCarbs; }
    public String getNotes()        { return notes; }

    // ── SETTERS ──────────────────────────────────────────────

    public void setMealType(String mealType)     { this.mealType = mealType; }
    public void setFood(String food)             { this.food = food; }
    public void setTargetCarbs(Integer v)        { this.targetCarbs = v; }
    public void setNotes(String notes)           { this.notes = notes; }
}