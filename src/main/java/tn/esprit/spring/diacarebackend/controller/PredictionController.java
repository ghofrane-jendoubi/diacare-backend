package tn.esprit.spring.diacarebackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.spring.diacarebackend.services.PredictionService;
import java.util.Map;

@RestController
@RequestMapping("/api/prediction")
@CrossOrigin(origins = "http://localhost:4200")
public class PredictionController {

    @Autowired
    private PredictionService predictionService;

    @PostMapping("/diabetes")
    public ResponseEntity<Map<String, Object>> predictDiabetes(@RequestBody Map<String, Object> patientData) {
        Map<String, Object> result = predictionService.predictDiabetes(patientData);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getModelInfo() {
        Map<String, Object> info = predictionService.getModelInfo();
        return ResponseEntity.ok(info);
    }
}