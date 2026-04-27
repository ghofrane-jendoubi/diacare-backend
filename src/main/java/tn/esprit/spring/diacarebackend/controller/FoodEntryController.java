package tn.esprit.spring.diacarebackend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.spring.diacarebackend.dto.FoodEntryRequest;
import tn.esprit.spring.diacarebackend.entities.FoodEntry;
import tn.esprit.spring.diacarebackend.services.FoodEntryService;
import tn.esprit.spring.diacarebackend.services.ImaggaService;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/foods")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class FoodEntryController {

    private final FoodEntryService service;
    private final ImaggaService imaggaService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Constructeur avec ImaggaService
    public FoodEntryController(FoodEntryService service,
                               ImaggaService imaggaService) {
        this.service = service;
        this.imaggaService = imaggaService;
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

    // ==================== IMAGE ANALYSIS WITH IMAGGA ====================

    @PostMapping("/analyze-image-ml")
    public ResponseEntity<Map<String, Object>> analyzeImageWithML(@RequestBody Map<String, String> body) {
        try {
            String base64 = body.get("image");
            Long patientId = Long.parseLong(body.get("patientId"));

            // 1️⃣ Imagga détecte les aliments dans l'image
            List<String> detectedFoods = imaggaService.detectFoodsFromBase64(base64);

            Map<String, Object> response = new HashMap<>();

            if (detectedFoods == null || detectedFoods.isEmpty()) {
                response.put("need_manual_input", true);
                response.put("message", "Je n'ai pas pu identifier les aliments. Pouvez-vous me décrire ce que vous avez mangé ? (ex: 'pizza and salad')");
                response.put("detected_foods", List.of());
                return ResponseEntity.ok(response);
            }

            // 2️⃣ Construire le texte pour votre ML
            String foodText = "I ate " + String.join(" and ", detectedFoods);
            System.out.println("🔍 Aliments détectés: " + detectedFoods);
            System.out.println("📝 Texte généré pour ML: " + foodText);

            // 3️⃣ Votre ML analyse le texte
            FoodEntry entry = service.addEntry(foodText, patientId);

            // 4️⃣ Extraire les informations nutritionnelles du résultat ML
            Map<String, Object> analysis = extractNutritionData(entry.getAnalysisResult());

            response.put("need_manual_input", false);
            response.put("detected_foods", detectedFoods);
            response.put("food_text", foodText);
            response.put("entry_id", entry.getId());
            response.put("analysis", analysis);

            // Extraire les totaux pour l'alerte glycémique
            double totalCarbs = 0;
            if (analysis.containsKey("total_carbs")) {
                totalCarbs = (double) analysis.get("total_carbs");
            }
            response.put("alert", totalCarbs > 45);
            response.put("alert_message", totalCarbs > 45 ?
                    "⚠️ Attention: " + totalCarbs + "g de glucides détectés! Limite recommandée: 45g par repas." : null);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Erreur lors de l'analyse: " + e.getMessage());
            errorResponse.put("need_manual_input", true);
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



    private Map<String, Object> extractNutritionData(String analysisResult) {
        Map<String, Object> result = new HashMap<>();
        try {
            JsonNode json = objectMapper.readTree(analysisResult);

            // Extraire les aliments détectés
            if (json.has("foods_detected")) {
                List<Map<String, Object>> foods = new java.util.ArrayList<>();
                for (JsonNode food : json.get("foods_detected")) {
                    Map<String, Object> foodMap = new HashMap<>();
                    foodMap.put("name", food.has("food") ? food.get("food").asText() : "unknown");
                    foodMap.put("calories", food.has("calories") ? food.get("calories").asDouble() : 0);
                    foodMap.put("carbs", food.has("carbs") ? food.get("carbs").asDouble() : 0);
                    foodMap.put("protein", food.has("protein") ? food.get("protein").asDouble() : 0);
                    foodMap.put("fat", food.has("fat") ? food.get("fat").asDouble() : 0);
                    foods.add(foodMap);
                }
                result.put("foods", foods);
            }

            // Extraire les totaux
            if (json.has("totals")) {
                JsonNode totals = json.get("totals");
                result.put("total_calories", totals.has("calories") ? totals.get("calories").asDouble() : 0);
                result.put("total_carbs", totals.has("carbs") ? totals.get("carbs").asDouble() : 0);
                result.put("total_protein", totals.has("protein") ? totals.get("protein").asDouble() : 0);
                result.put("total_fat", totals.has("fat") ? totals.get("fat").asDouble() : 0);
            }

        } catch (Exception e) {
            System.err.println("Erreur extraction données nutrition: " + e.getMessage());
        }
        return result;
    }
}