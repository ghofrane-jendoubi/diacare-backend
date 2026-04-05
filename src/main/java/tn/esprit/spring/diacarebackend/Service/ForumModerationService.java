package tn.esprit.spring.diacarebackend.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.*;

@Service
public class ForumModerationService {

    @Value("${gemini.api.key:demo}")
    private String geminiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, Object> moderateContent(String content) {
        try {
            if (geminiKey.equals("demo")) {
                return basicModeration(content);
            }

            String prompt = """
                Tu es un modérateur médical pour un forum diabétiques.
                Analyse ce contenu et réponds en JSON:
                {
                  "safe": true/false,
                  "score": 0.0-1.0,
                  "reason": "raison si dangereux",
                  "category": "safe/medical_advice/inappropriate/spam"
                }
                
                Règles:
                - Marquer false si: conseils médicaux dangereux, automédication, insultes
                - Marquer true si: partage d'expérience, recette, motivation, question générale
                
                Contenu à analyser: "%s"
                
                Réponds UNIQUEMENT avec le JSON.
                """.formatted(content);

            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-pro-latest:generateContent?key=" + geminiKey;

            Map<String, Object> request = new HashMap<>();
            List<Map<String, Object>> contents = new ArrayList<>();
            Map<String, Object> contentMap = new HashMap<>();
            List<Map<String, Object>> parts = new ArrayList<>();
            Map<String, Object> part = new HashMap<>();
            part.put("text", prompt);
            parts.add(part);
            contentMap.put("parts", parts);
            contents.add(contentMap);
            request.put("contents", contents);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    url, new HttpEntity<>(request, headers), Map.class);

            List<Map> candidates = (List<Map>) response.getBody().get("candidates");
            Map content2 = (Map) candidates.get(0).get("content");
            List<Map> parts2 = (List<Map>) content2.get("parts");
            String jsonText = (String) parts2.get(0).get("text");

            jsonText = jsonText.replaceAll("```json", "").replaceAll("```", "").trim();
            return new com.fasterxml.jackson.databind.ObjectMapper()
                    .readValue(jsonText, Map.class);

        } catch (Exception e) {
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