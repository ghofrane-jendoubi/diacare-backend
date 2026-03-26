package tn.esprit.spring.diacarebackend.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "diet_meals")
public class DietMeal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String mealType;     // breakfast, lunch, dinner, snack
    private String food;         // aliments recommandés
    private Integer targetCarbs; // glucides cibles pour ce repas
    private String notes;        // conseils du nutritionniste

    @ManyToOne
    @JoinColumn(name = "diet_plan_id")
    private DietPlan dietPlan;

    // ── GETTERS ──────────────────────────────────────────────

    public Long getId()              { return id; }
    public String getMealType()      { return mealType; }
    public String getFood()          { return food; }
    public Integer getTargetCarbs()  { return targetCarbs; }
    public String getNotes()         { return notes; }
    public DietPlan getDietPlan()    { return dietPlan; }

    // ── SETTERS ──────────────────────────────────────────────

    public void setId(Long id)              { this.id = id; }
    public void setMealType(String t)       { this.mealType = t; }
    public void setFood(String food)        { this.food = food; }
    public void setTargetCarbs(Integer v)   { this.targetCarbs = v; }
    public void setNotes(String notes)      { this.notes = notes; }
    public void setDietPlan(DietPlan plan)  { this.dietPlan = plan; }
}