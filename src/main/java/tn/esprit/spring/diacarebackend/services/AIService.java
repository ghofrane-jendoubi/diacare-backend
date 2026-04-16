package tn.esprit.spring.diacarebackend.services;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class AIService {

    private final RestTemplate restTemplate = new RestTemplate();

    public String analyzeFood(String text) {

        String url = "http://localhost:5000/api/detect-foods";

        Map<String, String> request = new HashMap<>();
        request.put("text", text);

        return restTemplate.postForObject(url, request, String.class);
    }
}