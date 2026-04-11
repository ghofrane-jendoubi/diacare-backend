package tn.esprit.spring.diacarebackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.spring.diacarebackend.dto.DiabetesPredictionRequest;
import tn.esprit.spring.diacarebackend.services.DiabetesPredictionService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/diabetes-prediction")
@CrossOrigin(origins = "http://localhost:4200")
public class DiabetesPredictionController {

    @Autowired
    private DiabetesPredictionService predictionService;

    @PostMapping("/predict")
    public ResponseEntity<Map<String, Object>> predict(@RequestBody DiabetesPredictionRequest request) {
        try {
            Map<String, Object> result = predictionService.predict(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}