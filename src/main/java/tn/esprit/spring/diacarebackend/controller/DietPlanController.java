package tn.esprit.spring.diacarebackend.controller;

import org.springframework.web.bind.annotation.*;
import tn.esprit.spring.diacarebackend.entities.*;
import tn.esprit.spring.diacarebackend.services.DietPlanService;

import java.util.List;

@RestController
@RequestMapping("/api/diet")
@CrossOrigin(origins = "http://localhost:4200")
public class DietPlanController {

    private final DietPlanService service;

    public DietPlanController(DietPlanService service) {
        this.service = service;
    }

    @PostMapping("/create")
    public DietPlan create(@RequestParam String title,
                           @RequestParam String description,
                           @RequestParam Long patientId,
                           @RequestParam Long nutritionistId) {

        return service.createPlan(title, description, patientId, nutritionistId);
    }

    @PostMapping("/add-meal")
    public DietMeal addMeal(@RequestParam Long planId,
                            @RequestParam String mealType,
                            @RequestParam String food) {

        return service.addMeal(planId, mealType, food);
    }

    @GetMapping("/patient/{id}")
    public List<DietPlan> getByPatient(@PathVariable Long id) {
        return service.getPlansByPatient(id);
    }
}