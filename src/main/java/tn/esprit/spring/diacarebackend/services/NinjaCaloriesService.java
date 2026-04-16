package tn.esprit.spring.diacarebackend.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
public class NinjaCaloriesService {

    @Value("${ninja.calories.token}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Détecte les aliments dans une image
     * Note: Ninja Calories n'a pas d'API de reconnaissance d'image
     * On utilise une approche avec des mots-clés basés sur les couleurs/formes
     * OU on retourne une liste pour que l'utilisateur choisisse
     */
    public List<String> detectFoodsFromImage(String base64Image) {
        // Pour l'instant, on retourne une liste d'aliments communs
        // L'utilisateur pourra sélectionner/modifier
        // Idéalement, utilisez une API comme Google Vision ou Clarifai

        // Solution temporaire: retourner une liste vide et l'utilisateur tape manuellement
        // Ou utiliser une librairie comme TensorFlow Lite

        System.out.println("📷 Analyse d'image reçue, longueur: " + base64Image.length());

        // TODO: Intégrer une vraie API de reconnaissance d'image
        // Option 1: Utiliser Clarifai (vous aviez déjà)
        // Option 2: Utiliser Google Cloud Vision
        // Option 3: Utiliser AWS Rekognition

        // Pour l'instant, on retourne null pour que le front demande à l'utilisateur
        return null;
    }
    // Ajoutez cette méthode à NinjaCaloriesService.java
    public Map<String, Object> getNutritionByFoodName(String foodName) {
        try {
            String url = "https://api.api-ninjas.com/v1/nutrition?query=" + foodName;

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Api-Key", apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    Map[].class
            );

            Map[] body = response.getBody();
            if (body != null && body.length > 0) {
                return body[0];
            }

            return new HashMap<>();

        } catch (Exception e) {
            System.err.println("❌ Ninja Calories API Error: " + e.getMessage());
            return new HashMap<>();
        }
    }

    /**
     * Alternative: Utiliser une API gratuite de reconnaissance d'image
     * Exemple avec Clarifai (gratuit: https://www.clarifai.com/)
     */
    public List<String> detectFoodsWithClarifai(String base64Image, String clarifaiApiKey) {
        try {
            String cleanBase64 = base64Image;
            if (base64Image.contains(",")) {
                cleanBase64 = base64Image.split(",")[1];
            }

            String url = "https://api.clarifai.com/v2/models/food-item-v1.0/outputs";

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Key " + clarifaiApiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("inputs", List.of(Map.of(
                    "data", Map.of("image", Map.of("base64", cleanBase64))
            )));

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            Map responseBody = response.getBody();
            List<String> foods = new ArrayList<>();

            if (responseBody != null) {
                List<Map<String, Object>> outputs = (List<Map<String, Object>>) responseBody.get("outputs");
                if (outputs != null && !outputs.isEmpty()) {
                    Map<String, Object> data = (Map<String, Object>) outputs.get(0).get("data");
                    List<Map<String, Object>> concepts = (List<Map<String, Object>>) data.get("concepts");

                    for (int i = 0; i < Math.min(5, concepts.size()); i++) {
                        String foodName = (String) concepts.get(i).get("name");
                        Double value = ((Number) concepts.get(i).get("value")).doubleValue();
                        if (value > 0.85) {
                            foods.add(foodName.toLowerCase());
                        }
                    }
                }
            }

            return foods;

        } catch (Exception e) {
            System.err.println("❌ Clarifai Error: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}