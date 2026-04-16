package tn.esprit.spring.diacarebackend.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.*;

@Service
public class ForumModerationService {

    @Value("${gemini.api.key:demo}")
    private String geminiKey;

    @Value("${openrouter.api.key:}")
    private String openRouterKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String OPENROUTER_URL = "https://openrouter.ai/api/v1/chat/completions";
    private static final String FREE_MODEL = "meta-llama/llama-3.2-3b-instruct:free";

    public Map<String, Object> moderateContent(String content) {
        // Mode offline : utiliser directement la modération basique (APIs rate-limited)
        return basicModeration(content);
    }

    private Map<String, Object> callOpenRouterModeration(String content) {
        if (openRouterKey == null || openRouterKey.isBlank()) {
            return basicModeration(content);
        }
        try {
            String prompt = """
                Tu es un modérateur médical pour un forum diabétiques.
                Analyse ce contenu et réponds UNIQUEMENT par SAFE ou DANGEROUS.
                
                Marque DANGEROUS si: conseils médicaux dangereux, automédication, insultes.
                Marque SAFE si: partage d'expérience, recette, motivation, question générale.
                
                Contenu: "%s"
                """.formatted(content);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + openRouterKey);
            headers.set("HTTP-Referer", "https://diacare.app");

            Map<String, Object> request = new HashMap<>();
            request.put("model", FREE_MODEL);
            request.put("messages", List.of(Map.of("role", "user", "content", prompt)));
            request.put("max_tokens", 50);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    OPENROUTER_URL, new HttpEntity<>(request, headers), Map.class);

            List<Map> choices = (List<Map>) response.getBody().get("choices");
            Map message = (Map) choices.get(0).get("message");
            String result = ((String) message.get("content")).toUpperCase();

            boolean isDangerous = result.contains("DANGEROUS");
            return Map.of(
                    "safe", !isDangerous,
                    "score", isDangerous ? 0.9 : 0.1,
                    "reason", isDangerous ? "Contenu potentiellement dangereux détecté par IA" : "",
                    "category", isDangerous ? "medical_advice" : "safe"
            );

        } catch (Exception e) {
            System.err.println("OpenRouter échoué: " + e.getMessage());
            return basicModeration(content);
        }
    }

    private Map<String, Object> basicModeration(String content) {
        String lower = content.toLowerCase();
        boolean dangerous = lower.contains("arrêter insuline") ||
                lower.contains("arreter insuline") ||
                lower.contains("ne pas prendre médicament") ||
                lower.contains("guérison miraculeuse") ||
                lower.contains("guerison miraculeuse");

        return Map.of(
                "safe", !dangerous,
                "score", dangerous ? 0.9 : 0.1,
                "reason", dangerous ? "Conseil médical potentiellement dangereux" : "",
                "category", dangerous ? "medical_advice" : "safe"
        );
    }
}
