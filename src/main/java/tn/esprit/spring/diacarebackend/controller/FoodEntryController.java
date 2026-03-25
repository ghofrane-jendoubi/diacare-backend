package tn.esprit.spring.diacarebackend.controller;

import org.springframework.web.bind.annotation.*;
import tn.esprit.spring.diacarebackend.dto.FoodEntryRequest;
import tn.esprit.spring.diacarebackend.entities.FoodEntry;
import tn.esprit.spring.diacarebackend.services.FoodEntryService;

import tn.esprit.spring.diacarebackend.services.LogMealService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/foods")
@CrossOrigin(origins = "*")
public class FoodEntryController {

    private final FoodEntryService service;
    private final LogMealService clarifaiService; // ← ajouter

    // ← injecter les 2 services dans constructor
    public FoodEntryController(FoodEntryService service,
                               LogMealService clarifaiService) {
        this.service = service;
        this.clarifaiService = clarifaiService;
    }

    @PostMapping
    public FoodEntry add(@RequestBody FoodEntryRequest request) {
        return service.addEntry(request.getText(), request.getPatientId());
    }

    @GetMapping
    public List<FoodEntry> getAll() {
        return service.getAll();
    }
    @GetMapping("/patient/{patientId}")
    public List<FoodEntry> getByPatient(@PathVariable Long patientId) {
        return service.getByPatientId(patientId);
    }
    // ← URL corrigée : juste "/analyze-image" pas "/api/foods/analyze-image"
    @PostMapping("/analyze-image")
    public FoodEntry analyzeImage(@RequestBody Map<String, String> body) {
        String base64 = body.get("image");
        List<String> foods = clarifaiService.detectFoods(base64);
        String text = "I ate " + String.join(" and ", foods);
        return service.addEntry(text, Long.parseLong(body.get("patientId")));
    }

}