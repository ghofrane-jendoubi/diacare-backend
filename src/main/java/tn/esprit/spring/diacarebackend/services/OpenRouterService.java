package tn.esprit.spring.diacarebackend.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Service alternatif utilisant OpenRouter API (modèles gratuits)
 * Fallback quand Gemini quota est épuisé
 */
@Service
public class OpenRouterService {

    @Value("${openrouter.api.key:}")
    private String openRouterKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String OPENROUTER_URL = "https://openrouter.ai/api/v1/chat/completions";

    /**
     * Modèle gratuit recommandé : meta-llama/llama-3.2-3b-instruct:free
     * Autres options gratuites :
     * - google/gemini-flash-1.5-8b:free
     * - huggingfaceh4/zephyr-7b-beta:free
     * - mistralai/mistral-7b-instruct:free
     */
    private static final String FREE_MODEL = "meta-llama/llama-3.2-3b-instruct:free";

    public String generateResponse(String prompt) {
        if (openRouterKey == null || openRouterKey.isBlank()) {
            return null; // Pas de clé, utiliser fallback
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + openRouterKey);
            headers.set("HTTP-Referer", "https://diacare.app"); // Requis par OpenRouter
            headers.set("X-Title", "DiaCare");

            Map<String, Object> request = new HashMap<>();
            request.put("model", FREE_MODEL);
            request.put("messages", List.of(
                Map.of("role", "system", "content", "Tu es DiaCare Assistant, un assistant médical spécialisé en diabétologie. Réponds UNIQUEMENT en français. Sois bienveillant, précis et professionnel. Limite tes réponses à 200 mots maximum."),
                Map.of("role", "user", "content", prompt)
            ));
            request.put("temperature", 0.7);
            request.put("max_tokens", 400);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                OPENROUTER_URL,
                new HttpEntity<>(request, headers),
                Map.class
            );

            List<Map> choices = (List<Map>) response.getBody().get("choices");
            Map message = (Map) choices.get(0).get("message");
            return (String) message.get("content");

        } catch (Exception e) {
            System.err.println("OpenRouter échoué: " + e.getMessage());
            return null; // Retourner null pour activer le fallback
        }
    }

    /**
     * Génération de recommandations pour le dashboard IA
     */
    public List<String> generateRecommendations(double moyenneGlycemie, String tendance) {
        String prompt = String.format("""
            Glycémie moyenne: %.2f g/L, tendance: %s.
            Donne exactement 4 recommandations courtes et pratiques en français pour un diabétique.
            Format: recommandation 1 | recommandation 2 | recommandation 3 | recommandation 4
            Réponds UNIQUEMENT avec les 4 recommandations séparées par | .
            """, moyenneGlycemie, tendance);

        String response = generateResponse(prompt);
        if (response == null) return null;

        // Parser la réponse
        String[] parts = response.split("\\|");
        List<String> recommendations = new ArrayList<>();
        for (String part : parts) {
            recommendations.add(part.trim());
        }
        return recommendations.size() >= 4 ? recommendations.subList(0, 4) : null;
    }
}
