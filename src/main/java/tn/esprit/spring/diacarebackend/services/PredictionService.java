package tn.esprit.spring.diacarebackend.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;

@Service
public class PredictionService {

    @Value("${prediction.api.url:http://localhost:5000}")
    private String predictionApiUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public PredictionService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public Map<String, Object> predictDiabetes(Map<String, Object> patientData) {
        try {
            // Validation des champs requis
            String[] requiredFields = {"Pregnancies", "Glucose", "BloodPressure",
                    "SkinThickness", "Insulin", "BMI", "DiabetesPedigreeFunction", "Age"};

            for (String field : requiredFields) {
                if (!patientData.containsKey(field)) {
                    throw new IllegalArgumentException("Champ manquant: " + field);
                }
            }

            // Appel à l'API Flask
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(patientData, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    predictionApiUrl + "/api/predict",
                    request,
                    Map.class
            );

            return response.getBody();

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return error;
        }
    }

    public Map<String, Object> getModelInfo() {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                    predictionApiUrl + "/api/info",
                    Map.class
            );
            return response.getBody();
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return error;
        }
    }
}