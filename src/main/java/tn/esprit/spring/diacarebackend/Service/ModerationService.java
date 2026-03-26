package tn.esprit.spring.diacarebackend.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
public class ModerationService {

    @Value("${gemini.api.key:demo}")
    private String geminiKey;
    private final RestTemplate restTemplate = new RestTemplate();

    public boolean isHarmfulContent(String text) {
        if ("demo".equals(geminiKey)) return false; // mode démo sans modération
        String prompt = "Analyse le texte suivant et réponds UNIQUEMENT par 'DANGEREUX' s'il contient un conseil médical dangereux (ex: arrêter un traitement, remplacer un médicament par un remède non prouvé, etc.). Sinon, réponds 'OK'.\n\nTexte : " + text;
        try {
            String response = callGemini(prompt);
            return response.toUpperCase().contains("DANGEREUX");
        } catch (Exception e) {
            return false;
        }
    }

    private String callGemini(String prompt) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=" + geminiKey;
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
}