package tn.esprit.spring.diacarebackend.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ImaggaService {

    @Value("${imagga.api.key}")
    private String apiKey;

    @Value("${imagga.api.secret}")
    private String apiSecret;

    @Value("${imagga.api.url:https://api.imagga.com/v2}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public List<String> detectFoodsFromBase64(String base64Image) {
        try {
            String cleanBase64 = base64Image;
            if (base64Image.contains(",")) {
                cleanBase64 = base64Image.split(",")[1];
            }

            byte[] imageBytes = Base64.getDecoder().decode(cleanBase64);

            String url = apiUrl + "/tags";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set("Authorization", getAuthHeader());

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("image", new org.springframework.core.io.ByteArrayResource(imageBytes) {
                @Override
                public String getFilename() {
                    return "food-image.jpg";
                }
            });

            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            return parseResponse(response.getBody());

        } catch (Exception e) {
            System.err.println("❌ Imagga Error: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> parseResponse(Map<String, Object> responseBody) {
        List<String> foods = new ArrayList<>();

        // Liste des mots-clés alimentaires
        Set<String> foodKeywords = new HashSet<>(Arrays.asList(
                "pizza", "burger", "sandwich", "pasta", "rice", "salad", "soup",
                "cake", "cookie", "donut", "pastry", "bread", "croissant",
                "fries", "chips", "potato", "chicken", "beef", "fish", "meat",
                "apple", "banana", "orange", "fruit", "vegetable", "egg",
                "cheese", "yogurt", "milk", "coffee", "tea", "juice", "soda",
                "chocolate", "dessert", "ice cream", "tomato", "pepperoni", "sauce"
        ));

        if (responseBody != null && responseBody.containsKey("result")) {
            Map<String, Object> result = (Map<String, Object>) responseBody.get("result");
            List<Map<String, Object>> tags = (List<Map<String, Object>>) result.get("tags");

            System.out.println("🏷️ Tags reçus: " + tags.size());

            for (Map<String, Object> tag : tags) {
                // ✅ CORRECTION IMPORTANTE : tag.get("tag") est un Map, pas un String
                Map<String, Object> tagMap = (Map<String, Object>) tag.get("tag");
                String tagName = ((String) tagMap.get("en")).toLowerCase();
                Double confidence = ((Number) tag.get("confidence")).doubleValue();

                System.out.println("  - " + tagName + " (confiance: " + confidence + "%)");

                if (confidence > 40.0 && isFoodKeyword(tagName, foodKeywords)) {
                    foods.add(tagName);
                    System.out.println("    ✅ AJOUTÉ: " + tagName);
                }
            }
        }

        List<String> result = foods.stream().distinct().limit(5).collect(Collectors.toList());
        System.out.println("🎯 Aliments finaux détectés: " + result);

        return result;
    }

    private boolean isFoodKeyword(String tag, Set<String> foodKeywords) {
        for (String keyword : foodKeywords) {
            if (tag.equals(keyword) || tag.contains(keyword) || keyword.contains(tag)) {
                return true;
            }
        }
        return false;
    }

    private String getAuthHeader() {
        String auth = apiKey + ":" + apiSecret;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        return "Basic " + encodedAuth;
    }
}