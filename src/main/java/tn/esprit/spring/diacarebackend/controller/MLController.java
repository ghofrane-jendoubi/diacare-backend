package tn.esprit.spring.diacarebackend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpServerErrorException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ml")
@CrossOrigin(origins = "http://localhost:4200")
public class MLController {

    private static final Logger log = LoggerFactory.getLogger(MLController.class);
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${ml.service.url:http://localhost:5000}")
    private String mlServiceUrl;

    @Value("${food.recognition.url:http://localhost:8001}")
    private String foodRecognitionUrl;

    @Value("${openfoodfacts.username:}")
    private String openfoodUsername;

    @Value("${openfoodfacts.password:}")
    private String openfoodPassword;

    // ==================== BARCODE ANALYSIS (RELIABLE) ====================
    @GetMapping("/analyze-barcode/{barcode}")
    public ResponseEntity<Map<String, Object>> analyzeByBarcode(@PathVariable String barcode) {
        Map<String, Object> response = new HashMap<>();
        try {
            String url = "https://world.openfoodfacts.org/api/v0/product/" + barcode + ".json";
            log.info("📷 Scanning barcode: {}", barcode);

            ResponseEntity<Map> apiResponse = restTemplate.getForEntity(url, Map.class);
            if (apiResponse.getBody() == null) {
                response.put("error", "No response from API");
                return ResponseEntity.status(500).body(response);
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> product = (Map<String, Object>) apiResponse.getBody().get("product");
            if (product == null) {
                response.put("error", "Product not found for barcode: " + barcode);
                return ResponseEntity.status(404).body(response);
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> nutriments = (Map<String, Object>) product.get("nutriments");

            Map<String, Double> nutritionData = extractNutritionData(nutriments);
            Map<String, Object> mlResult = callMLService(nutritionData);

            response.put("productName", product.get("product_name"));
            response.put("brand", product.get("brands"));
            response.put("nutrition", nutritionData);
            response.put("recommendation", mlResult);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error: {}", e.getMessage(), e);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // ==================== NAME SEARCH (FALLBACK, WITH ERROR HANDLING) ====================
    @PostMapping("/analyze-name")
    public ResponseEntity<Map<String, Object>> analyzeByName(@RequestBody Map<String, String> request) {
        String productName = request.get("productName").trim();
        Map<String, Object> response = new HashMap<>();

        try {
            String encodedName = URLEncoder.encode(productName, StandardCharsets.UTF_8);
            StringBuilder searchUrlBuilder = new StringBuilder(
                    "https://world.openfoodfacts.org/cgi/search.pl?search_terms=" + encodedName +
                            "&search_simple=1&action=process&json=1&page_size=1"
            );

            // Add credentials if provided
            if (openfoodUsername != null && !openfoodUsername.isEmpty() &&
                    openfoodPassword != null && !openfoodPassword.isEmpty()) {
                searchUrlBuilder.append("&user_id=").append(openfoodUsername)
                        .append("&password=").append(openfoodPassword);
                log.info("Using authenticated Open Food Facts account");
            }

            String searchUrl = searchUrlBuilder.toString();

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "DiacareApp/1.0 (contact@diacare.com)");
            headers.set("Accept", "application/json");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> searchResponse = null;
            int retries = 3;
            for (int i = 0; i < retries; i++) {
                try {
                    searchResponse = restTemplate.exchange(searchUrl, HttpMethod.GET, entity, Map.class);
                    if (searchResponse.getStatusCode().is2xxSuccessful()) {
                        break;
                    }
                } catch (HttpServerErrorException.ServiceUnavailable e) {
                    log.warn("Open Food Facts search unavailable, attempt {}/{}", i + 1, retries);
                    Thread.sleep(2000);
                }
            }

            // If after retries we still have no valid response, return a warning (200 OK)
            if (searchResponse == null || searchResponse.getBody() == null) {
                response.put("warning", "Le service de recherche est temporairement indisponible. Veuillez utiliser le scan de code-barres.");
                response.put("productName", productName);
                response.put("nutrition", getEmptyNutrition());
                response.put("recommendation", Map.of(
                        "message", "Analyse non disponible – scannez le code-barres",
                        "is_recommended", false
                ));
                return ResponseEntity.ok(response);
            }

            // Extract products
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> products = (List<Map<String, Object>>) searchResponse.getBody().get("products");
            if (products == null || products.isEmpty()) {
                response.put("warning", "Produit non trouvé. Veuillez vérifier le nom ou utiliser le scan de code-barres.");
                response.put("productName", productName);
                response.put("nutrition", getEmptyNutrition());
                response.put("recommendation", Map.of(
                        "message", "Produit non trouvé – scannez le code-barres",
                        "is_recommended", false
                ));
                return ResponseEntity.ok(response);
            }

            String barcode = (String) products.get(0).get("code");
            if (barcode == null) {
                response.put("warning", "Code-barres introuvable. Utilisez le scan de code-barres.");
                response.put("productName", productName);
                response.put("nutrition", getEmptyNutrition());
                response.put("recommendation", Map.of(
                        "message", "Code-barres manquant – scannez le code-barres",
                        "is_recommended", false
                ));
                return ResponseEntity.ok(response);
            }

            // Fetch full product data using the barcode (reliable endpoint)
            String barcodeUrl = "https://world.openfoodfacts.org/api/v0/product/" + barcode + ".json";
            ResponseEntity<Map> barcodeResponse = restTemplate.getForEntity(barcodeUrl, Map.class);
            if (barcodeResponse.getBody() == null) {
                response.put("error", "Impossible de récupérer les données nutritionnelles.");
                return ResponseEntity.status(500).body(response);
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> product = (Map<String, Object>) barcodeResponse.getBody().get("product");
            if (product == null) {
                response.put("error", "Produit non trouvé pour ce code-barres.");
                return ResponseEntity.status(404).body(response);
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> nutriments = (Map<String, Object>) product.get("nutriments");
            if (nutriments == null) {
                response.put("error", "Données nutritionnelles manquantes.");
                return ResponseEntity.status(404).body(response);
            }

            Map<String, Double> nutritionData = extractNutritionData(nutriments);
            Map<String, Object> mlResult = callMLService(nutritionData);

            response.put("productName", product.get("product_name"));
            response.put("brand", product.get("brands"));
            response.put("barcode", barcode);
            response.put("nutrition", nutritionData);
            response.put("recommendation", mlResult);
            return ResponseEntity.ok(response);

        } catch (HttpServerErrorException.ServiceUnavailable e) {
            // Open Food Facts search API is down – return 200 with warning
            response.put("warning", "Open Food Facts est temporairement indisponible. Veuillez utiliser le scan de code-barres.");
            response.put("productName", productName);
            response.put("nutrition", getEmptyNutrition());
            response.put("recommendation", Map.of(
                    "message", "Analyse non disponible – scannez le code-barres",
                    "is_recommended", false
            ));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Unexpected error in analyzeByName", e);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // Helper to return empty nutrition (zero values)
    private Map<String, Double> getEmptyNutrition() {
        Map<String, Double> empty = new HashMap<>();
        empty.put("sugars_100g", 0.0);
        empty.put("fat_100g", 0.0);
        empty.put("carbohydrates_100g", 0.0);
        empty.put("proteins_100g", 0.0);
        empty.put("fiber_100g", 0.0);
        empty.put("sodium_100g", 0.0);
        return empty;
    }

    // ==================== IMAGE ANALYSIS ====================
    @PostMapping("/analyze-image")
    public ResponseEntity<Map<String, Object>> analyzeByImage(@RequestParam("image") MultipartFile image) {
        Map<String, Object> response = new HashMap<>();
        try {
            String foodName = callFoodRecognitionService(image);
            if (foodName == null || foodName.isEmpty()) {
                response.put("error", "Aucun aliment reconnu dans l'image");
                return ResponseEntity.status(404).body(response);
            }

            // Reuse the name search logic (which already handles errors)
            Map<String, String> nameRequest = Map.of("productName", foodName);
            ResponseEntity<Map<String, Object>> nameResult = analyzeByName(nameRequest);
            return nameResult;  // forward the response

        } catch (Exception e) {
            log.error("Error analyzing image: {}", e.getMessage(), e);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // ==================== ML SERVICE ====================
    private Map<String, Object> callMLService(Map<String, Double> nutritionData) {
        try {
            String url = mlServiceUrl + "/predict";
            log.info("🤖 Calling ML service at: {}", url);
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            HttpEntity<Map<String, Double>> entity = new HttpEntity<>(nutritionData, headers);
            @SuppressWarnings("unchecked")
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            log.info("✅ ML Response: {}", response.getBody());
            return response.getBody();
        } catch (Exception e) {
            log.error("❌ ML Service error: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("error", "ML Service unavailable");
            error.put("message", "Service d'analyse temporairement indisponible");
            error.put("prediction", -1);
            error.put("is_recommended", false);
            return error;
        }
    }

    // ==================== FOOD RECOGNITION SERVICE (YOLO) ====================
    private String callFoodRecognitionService(MultipartFile image) throws Exception {
        String url = foodRecognitionUrl + "/recognize";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(image.getBytes()) {
            @Override
            public String getFilename() {
                return image.getOriginalFilename();
            }
        });
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
        if (response.getBody() != null && Boolean.TRUE.equals(response.getBody().get("success"))) {
            return (String) response.getBody().get("food_name");
        }
        return null;
    }

    // ==================== OPEN FOOD FACTS HELPERS ====================
    private Map<String, Double> extractNutritionData(Map<String, Object> nutriments) {
        Map<String, Double> nutrition = new HashMap<>();
        nutrition.put("sugars_100g", getDoubleValue(nutriments, "sugars_100g"));
        nutrition.put("fat_100g", getDoubleValue(nutriments, "fat_100g"));
        nutrition.put("carbohydrates_100g", getDoubleValue(nutriments, "carbohydrates_100g"));
        nutrition.put("saturated_fat_100g", getDoubleValue(nutriments, "saturated-fat_100g"));
        nutrition.put("fiber_100g", getDoubleValue(nutriments, "fiber_100g"));
        nutrition.put("proteins_100g", getDoubleValue(nutriments, "proteins_100g"));
        nutrition.put("sodium_100g", getDoubleValue(nutriments, "sodium_100g"));
        return nutrition;
    }

    private double getDoubleValue(Map<String, Object> map, String key) {
        if (map == null) return 0.0;
        Object value = map.get(key);
        if (value == null) return 0.0;
        if (value instanceof Number) return ((Number) value).doubleValue();
        return 0.0;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        return ResponseEntity.ok(response);
    }
}