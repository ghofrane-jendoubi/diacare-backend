package tn.esprit.spring.diacarebackend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.spring.diacarebackend.dto.FoodEntryRequest;
import tn.esprit.spring.diacarebackend.entities.FoodEntry;
import tn.esprit.spring.diacarebackend.services.FoodEntryService;
import tn.esprit.spring.diacarebackend.services.NinjaCaloriesService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/foods")
@CrossOrigin(origins = "http://localhost:4200")
public class FoodEntryController {

    private final FoodEntryService service;
    private final NinjaCaloriesService ninjaCaloriesService;

    // Constructeur sans LogMealService
    public FoodEntryController(FoodEntryService service,
                               NinjaCaloriesService ninjaCaloriesService) {
        this.service = service;
        this.ninjaCaloriesService = ninjaCaloriesService;
    }

    // ==================== TEXT ANALYSIS ====================

    @PostMapping
    public ResponseEntity<FoodEntry> add(@RequestBody FoodEntryRequest request) {
        try {
            FoodEntry entry = service.addEntry(request.getText(), request.getPatientId());
            return ResponseEntity.ok(entry);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== GET METHODS ====================

    @GetMapping
    public ResponseEntity<List<FoodEntry>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<FoodEntry>> getByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(service.getByPatientId(patientId));
    }

    @GetMapping("/patient/{patientId}/today")
    public ResponseEntity<List<FoodEntry>> getTodayByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(service.getTodayEntries(patientId));
    }

    // ==================== IMAGE ANALYSIS ====================

    @PostMapping("/analyze-image")
    public ResponseEntity<Map<String, Object>> analyzeImage(@RequestBody Map<String, String> body) {
        try {
            String base64 = body.get("image");
            Long patientId = Long.parseLong(body.get("patientId"));

            // Pour l'instant, demander à l'utilisateur de décrire
            // Plus tard, intégrer une API de reconnaissance d'image
            Map<String, Object> response = new HashMap<>();
            response.put("need_manual_input", true);
            response.put("message", "📷 Photo reçue ! Pouvez-vous me décrire ce que vous avez mangé ? (ex: 'pizza and salad')");
            response.put("detected_foods", List.of());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Erreur lors de l'analyse: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    // ==================== DELETE ====================

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            service.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }
}