package tn.esprit.spring.diacarebackend.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class AIService {

    private static final Logger log = LoggerFactory.getLogger(AIService.class);
    private final RestTemplate restTemplate = new RestTemplate();

    public String analyzeFood(String text) {
        String url = "http://127.0.0.1:5000/api/detect-foods";

        log.info("=== Appel au service ML ===");
        log.info("URL: {}", url);
        log.info("Texte à analyser: {}", text);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> request = new HashMap<>();
            request.put("text", text);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

            log.info("Envoi de la requête à Flask...");
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            log.info("Réponse reçue - Status: {}", response.getStatusCode());
            log.info("Réponse body: {}", response.getBody());

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            } else {
                log.error("Réponse invalide de Flask: {}", response.getStatusCode());
                return createErrorResponse(text, "Flask returned status: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("Erreur lors de l'appel à Flask: {}", e.getMessage());
            e.printStackTrace();
            return createErrorResponse(text, e.getMessage());
        }
    }

    private String createErrorResponse(String text, String error) {
        try {
            Map<String, Object> errorResponse = new HashMap<>();

            // Créer une réponse mock pour ne pas bloquer l'utilisateur
            Map<String, Object> food = new HashMap<>();
            food.put("food", text.contains("pizza") ? "pizza" :
                    text.contains("salad") ? "salad" :
                            text.contains("chicken") ? "chicken" : "food");
            food.put("calories", 200);
            food.put("carbs", 25);
            food.put("protein", 10);
            food.put("fat", 8);
            food.put("fibre", 2);

            java.util.List<Map<String, Object>> foods = new java.util.ArrayList<>();
            foods.add(food);

            errorResponse.put("foods_detected", foods);
            errorResponse.put("text", text);
            errorResponse.put("analysis_date", LocalDateTime.now().toString());
            errorResponse.put("warning", "ML service error: " + error);

            Map<String, Object> totals = new HashMap<>();
            totals.put("calories", 200);
            totals.put("carbs", 25);
            totals.put("protein", 10);
            totals.put("fat", 8);
            errorResponse.put("totals", totals);

            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.writeValueAsString(errorResponse);

        } catch (Exception e) {
            return "{\"foods_detected\":[], \"error\":\"" + error + "\"}";
        }
    }
}