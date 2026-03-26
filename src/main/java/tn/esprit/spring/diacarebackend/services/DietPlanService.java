package tn.esprit.spring.diacarebackend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.spring.diacarebackend.dto.DietPlanRequest;
import tn.esprit.spring.diacarebackend.dto.MealRequest;   // ← import manquant !
import tn.esprit.spring.diacarebackend.entities.DietMeal;
import tn.esprit.spring.diacarebackend.entities.DietPlan;
import tn.esprit.spring.diacarebackend.repository.DietPlanRepository;
import tn.esprit.spring.diacarebackend.repository.UserRepository;

import java.util.List;

@Service
public class DietPlanService {

    @Autowired
    private DietPlanRepository dietPlanRepository;

    @Autowired
    private UserRepository userRepository;

    public DietPlan createPlan(DietPlanRequest req) {
        DietPlan plan = new DietPlan();
        plan.setTitle(req.getTitle());
        plan.setDescription(req.getDescription());
        plan.setTargetCalories(req.getTargetCalories());
        plan.setTargetCarbs(req.getTargetCarbs());
        plan.setTargetProtein(req.getTargetProtein());
        plan.setTargetFat(req.getTargetFat());
        plan.setStatus("active");

        // Patient (id depuis request)
        if (req.getPatientId() != null) {
            userRepository.findById(req.getPatientId())
                    .ifPresent(plan::setPatient);
        }

        // Nutritionniste (id depuis request)
        if (req.getNutritionistId() != null) {
            userRepository.findById(req.getNutritionistId())
                    .ifPresent(plan::setNutritionist);
        }

        // Repas associés
        if (req.getMeals() != null) {
            for (MealRequest m : req.getMeals()) {
                DietMeal meal = new DietMeal();
                meal.setMealType(m.getMealType());
                meal.setFood(m.getFood());
                meal.setTargetCarbs(m.getTargetCarbs());
                meal.setNotes(m.getNotes());
                meal.setDietPlan(plan);          // ← lien bidirectionnel
                plan.getMeals().add(meal);
            }
        }

        return dietPlanRepository.save(plan);
    }

    public List<DietPlan> getByPatientId(Long patientId) {
        return dietPlanRepository.findByPatientId(patientId);
    }

    public List<DietPlan> getAllPlans() {
        return dietPlanRepository.findAll();
    }
}