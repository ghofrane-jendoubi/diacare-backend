package tn.esprit.spring.diacarebackend.dto;

public class DietMealRequest {
    private Long dietPlanId;
    private String mealType;
    private String food;
    private Integer calories;
    private String notes;

    // getters & setters
    public Long getDietPlanId() { return dietPlanId; }
    public void setDietPlanId(Long dietPlanId) { this.dietPlanId = dietPlanId; }

    public String getMealType() { return mealType; }
    public void setMealType(String mealType) { this.mealType = mealType; }

    public String getFood() { return food; }
    public void setFood(String food) { this.food = food; }

    public Integer getCalories() { return calories; }
    public void setCalories(Integer calories) { this.calories = calories; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
