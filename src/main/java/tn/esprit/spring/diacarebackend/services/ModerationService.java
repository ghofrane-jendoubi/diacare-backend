package tn.esprit.spring.diacarebackend.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
public class ModerationService {

    @Value("${gemini.api.key:demo}")
    private String geminiKey;

    @Value("${openrouter.api.key:}")
    private String openRouterKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String OPENROUTER_URL = "https://openrouter.ai/api/v1/chat/completions";
    private static final String FREE_MODEL = "meta-llama/llama-3.2-3b-instruct:free";

    public boolean isHarmfulContent(String text) {
        // Mode offline : pas de modération IA (APIs rate-limited)
        // Le contenu est considéré comme sûr par défaut
        return false;
    }

    private String callGemini(String prompt) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + geminiKey;
        Map<String, Object> request = new HashMap<>();
        request.put("contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))));
        request.put("generationConfig", Map.of("temperature", 0.2, "maxOutputTokens", 50));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                url, new HttpEntity<>(request, headers), Map.class);

        List<Map> candidates = (List<Map>) response.getBody().get("candidates");
        Map content = (Map) candidates.get(0).get("content");
        List<Map> parts = (List<Map>) content.get("parts");
        return (String) parts.get(0).get("text");
    }

    private String callOpenRouter(String prompt) {
        if (openRouterKey == null || openRouterKey.isBlank()) {
            throw new RuntimeException("OpenRouter key not configured");
        }
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
        return (String) message.get("content");
    }
}
