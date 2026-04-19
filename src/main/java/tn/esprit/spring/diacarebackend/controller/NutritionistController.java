// NutritionistController.java
package tn.esprit.spring.diacarebackend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.spring.diacarebackend.services.NutritionistService;

import java.util.Map;

@RestController
@RequestMapping("/api/nutritionnists")
@CrossOrigin(origins = "http://localhost:4200")
public class NutritionistController {

    private final NutritionistService nutritionistService;

    public NutritionistController(NutritionistService nutritionistService) {
        this.nutritionistService = nutritionistService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            Map<String, Object> nutritionist = nutritionistService.getNutritionistById(id);
            if (nutritionist == null || nutritionist.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(nutritionist);
        } catch (Exception e) {
            System.err.println("Erreur getNutritionistById: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}