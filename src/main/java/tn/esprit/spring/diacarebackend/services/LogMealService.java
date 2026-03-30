package tn.esprit.spring.diacarebackend.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
public class LogMealService {

    @Value("${spoonacular.token}")
    private String token;

    private final RestTemplate restTemplate = new RestTemplate();

    public List<String> detectFoods(String base64Image) {
        try {
            String cleanBase64 = base64Image;

            if (base64Image.contains(",")) {
                cleanBase64 = base64Image.split(",")[1];
            }

            // Nettoyage
            cleanBase64 = cleanBase64.replaceAll("\\s", "");

            // URL safe → standard
            cleanBase64 = cleanBase64
                    .replace('-', '+')
                    .replace('_', '/');

            // Fix padding
            int padding = cleanBase64.length() % 4;
            if (padding != 0) {
                cleanBase64 += "=".repeat(4 - padding);
            }

            byte[] imageBytes = Base64.getDecoder().decode(cleanBase64);

            String url = "https://api.logmeal.com/v2/image/segmentation/complete";

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("image", new ByteArrayResource(imageBytes) {
                @Override
                public String getFilename() {
                    return "image.jpg";
                }
            });

            HttpEntity<MultiValueMap<String, Object>> request =
                    new HttpEntity<>(body, headers);

            ResponseEntity<Map> response =
                    restTemplate.postForEntity(url, request, Map.class);

            Set<String> foodsSet = new HashSet<>();

            Map responseBody = response.getBody();

            if (responseBody != null && responseBody.containsKey("segmentation_results")) {

                List<Map<String, Object>> results =
                        (List<Map<String, Object>>) responseBody.get("segmentation_results");

                for (Map<String, Object> result : results) {

                    if (result.containsKey("recognition_results")) {

                        List<Map<String, Object>> recognitions =
                                (List<Map<String, Object>>) result.get("recognition_results");

                        for (Map<String, Object> rec : recognitions) {

                            String foodName = (String) rec.get("name");
                            Double confidence = rec.containsKey("prob")
                                    ? (Double) rec.get("prob") : 1.0;

                            if (confidence > 0.6) {
                                foodsSet.add(foodName.toLowerCase());
                            }
                        }
                    }
                }
            }

            return foodsSet.isEmpty()
                    ? List.of("unknown food")
                    : foodsSet.stream().limit(5).toList();

        } catch (Exception e) {
            System.err.println("❌ LogMeal Error: " + e.getMessage());
            return List.of("error");
        }
    }
}
