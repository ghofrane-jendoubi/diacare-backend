package tn.esprit.spring.diacarebackend.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;

import java.util.ArrayList;
import java.util.List;   // ← java.util.List PAS org.hibernate.mapping.List !

@Entity
@Table(name = "diet_plans")
public class DietPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private String status;// "active", "completed", "paused"



    private String startDate;

    private String endDate;

    private Integer targetCalories;
    private Integer targetCarbs;
    private Integer targetProtein;
    private Integer targetFat;



    @ManyToOne
    @JoinColumn(name = "patient_id")
    private User patient;

    @ManyToOne
    @JoinColumn(name = "nutritionist_id")
    private User nutritionist;

    // ← CascadeType.ALL + orphanRemoval pour supprimer les repas avec le plan
    @OneToMany(mappedBy = "dietPlan", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.EAGER)
    private List<DietMeal> meals = new ArrayList<>();

    // ── GETTERS ──────────────────────────────────────────────

    public Long getId()               { return id; }
    public String getTitle()          { return title; }
    public String getDescription()    { return description; }
    public String getStatus()         { return status; }
    public String getStartDate()      { return startDate; }
    public String getEndDate()        { return endDate; }
    public Integer getTargetCalories(){ return targetCalories; }
    public Integer getTargetCarbs()   { return targetCarbs; }
    public Integer getTargetProtein() { return targetProtein; }
    public Integer getTargetFat()     { return targetFat; }
    public User getPatient()          { return patient; }
    public User getNutritionist()     { return nutritionist; }
    public List<DietMeal> getMeals()  { return meals; }

    // ── SETTERS ──────────────────────────────────────────────

    public void setId(Long id)                      { this.id = id; }
    public void setTitle(String title)              { this.title = title; }
    public void setDescription(String description)  { this.description = description; }
    public void setStatus(String status)            { this.status = status; }
    public void setStartDate(String startDate)      { this.startDate = startDate; }
    public void setEndDate(String endDate)          { this.endDate = endDate; }
    public void setTargetCalories(Integer v)        { this.targetCalories = v; }
    public void setTargetCarbs(Integer v)           { this.targetCarbs = v; }
    public void setTargetProtein(Integer v)         { this.targetProtein = v; }
    public void setTargetFat(Integer v)             { this.targetFat = v; }
    public void setPatient(User patient)            { this.patient = patient; }
    public void setNutritionist(User nutritionist)  { this.nutritionist = nutritionist; }
    public void setMeals(List<DietMeal> meals)      { this.meals = meals; }
}