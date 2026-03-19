package tn.esprit.spring.diacarebackend.entities;

import jakarta.persistence.*;

@Entity
public class DietMeal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String mealType;
    private String food;

    @ManyToOne
    private DietPlan dietPlan;

    // 🔹 GETTERS

    public Long getId() {
        return id;
    }

    public String getMealType() {
        return mealType;
    }

    public String getFood() {
        return food;
    }

    public DietPlan getDietPlan() {
        return dietPlan;
    }

    // 🔹 SETTERS

    public void setId(Long id) {
        this.id = id;
    }

    public void setMealType(String mealType) {
        this.mealType = mealType;
    }

    public void setFood(String food) {
        this.food = food;
    }

    public void setDietPlan(DietPlan dietPlan) {
        this.dietPlan = dietPlan;
    }
}