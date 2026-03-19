package tn.esprit.spring.diacarebackend.services;

import org.springframework.stereotype.Service;
import tn.esprit.spring.diacarebackend.entities.*;
import tn.esprit.spring.diacarebackend.repository.*;

import java.util.List;

@Service
public class DietPlanService {

    private final DietPlanRepository dietRepo;
    private final DietMealRepository mealRepo;
    private final UserRepository userRepository;

    public DietPlanService(DietPlanRepository dietRepo,
                           DietMealRepository mealRepo,
                           UserRepository userRepository) {
        this.dietRepo = dietRepo;
        this.mealRepo = mealRepo;
        this.userRepository = userRepository;
    }

    // ✅ créer plan
    public DietPlan createPlan(String title, String description,
                               Long patientId, Long nutritionistId) {

        User patient = userRepository.findById(patientId).orElseThrow();
        User nutritionist = userRepository.findById(nutritionistId).orElseThrow();

        DietPlan plan = new DietPlan();
        plan.setTitle(title);
        plan.setDescription(description);
        plan.setPatient(patient);
        plan.setNutritionist(nutritionist);

        return dietRepo.save(plan);
    }

    // ✅ ajouter repas
    public DietMeal addMeal(Long planId, String mealType, String food) {

        DietPlan plan = dietRepo.findById(planId).orElseThrow();

        DietMeal meal = new DietMeal();
        meal.setMealType(mealType);
        meal.setFood(food);
        meal.setDietPlan(plan);

        return mealRepo.save(meal);
    }

    // ✅ récupérer plans patient
    public List<DietPlan> getPlansByPatient(Long patientId) {
        return dietRepo.findAll()
                .stream()
                .filter(p -> p.getPatient().getId().equals(patientId))
                .toList();
    }
}