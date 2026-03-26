package tn.esprit.spring.diacarebackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.spring.diacarebackend.dto.DietPlanRequest;
import tn.esprit.spring.diacarebackend.entities.*;
import tn.esprit.spring.diacarebackend.services.DietPlanService;

import java.util.List;

@RestController
@RequestMapping("/api/diet")
@CrossOrigin(origins = "*")
public class DietPlanController {

    @Autowired
    private DietPlanService dietPlanService;

    // Créer un plan complet avec ses repas
    @PostMapping("/create")
    public ResponseEntity<DietPlan> createPlan(@RequestBody DietPlanRequest request) {
        return ResponseEntity.ok(dietPlanService.createPlan(request));
    }

    // Récupérer les plans d'un patient
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<DietPlan>> getByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(dietPlanService.getByPatientId(patientId));
    }

    // Récupérer tous les plans du nutritionniste
    @GetMapping("/my-plans")
    public ResponseEntity<List<DietPlan>> getMyPlans() {
        return ResponseEntity.ok(dietPlanService.getAllPlans());
    }
}