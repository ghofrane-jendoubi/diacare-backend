package tn.esprit.spring.diacarebackend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private final Map<String, Map<String, Object>> productCache = new HashMap<>();


    @Value("${ml.service.url:http://localhost:5000}")
    private String mlServiceUrl;

    @Value("${food.recognition.url:http://localhost:8001}")
    private String foodRecognitionUrl;

    // 🔐 Identifiants Open Food Facts (à stocker dans application.properties)
    @Value("${openfoodfacts.username:}")
    private String openfoodUsername;

    @Value("${openfoodfacts.password:}")
    private String openfoodPassword;

    // ==================== BARCODE ANALYSIS ====================
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

    // ==================== NAME SEARCH ====================
    @PostMapping("/analyze-name")
    public ResponseEntity<Map<String, Object>> analyzeByName(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String productName = request.get("productName");
            log.info("🔍 Searching by name: {}", productName);

            String encodedName = URLEncoder.encode(productName, StandardCharsets.UTF_8);

            StringBuilder searchUrlBuilder = new StringBuilder(
                    "https://world.openfoodfacts.org/cgi/search.pl?search_terms=" + encodedName +
                            "&search_simple=1&action=process&json=1&page_size=1"
            );

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

            Thread.sleep(1000);

            ResponseEntity<Map> searchResponse = null;
            int retries = 3;
            for (int i = 0; i < retries; i++) {
                try {
                    searchResponse = restTemplate.exchange(searchUrl, HttpMethod.GET, entity, Map.class);
                    if (searchResponse.getStatusCode().is2xxSuccessful()) {
                        break;
                    }
                } catch (HttpServerErrorException.ServiceUnavailable e) {
                    log.warn("Rate limited, attempt {}/{}", i + 1, retries);
                    Thread.sleep(3000);
                }
            }

            if (searchResponse == null || searchResponse.getBody() == null) {
                response.put("error", "Service temporairement indisponible. Veuillez utiliser le scan de code-barres.");
                response.put("suggestion", "Le scan de code-barres est plus fiable.");
                return ResponseEntity.status(503).body(response);
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> products = (List<Map<String, Object>>) searchResponse.getBody().get("products");

            if (products == null || products.isEmpty()) {
                response.put("error", "Product not found: " + productName);
                response.put("suggestion", "Essayez le scan de code-barres pour de meilleurs résultats");
                return ResponseEntity.status(404).body(response);
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> product = products.get(0);

            String barcode = product.get("code") != null ? product.get("code").toString() : null;

            if (barcode == null) {
                response.put("productName", product.get("product_name"));
                response.put("brand", product.get("brands"));
                response.put("warning", "Utilisez le scan de code-barres pour une analyse complète");
                response.put("recommendation", Map.of(
                        "message", "Utilisez le scan de code-barres pour une analyse complète",
                        "is_recommended", false
                ));
                return ResponseEntity.ok(response);
            }

            String barcodeUrl = "https://world.openfoodfacts.org/api/v0/product/" + barcode + ".json";
            ResponseEntity<Map> barcodeResponse = restTemplate.getForEntity(barcodeUrl, Map.class);

            if (barcodeResponse.getBody() == null) {
                response.put("error", "Could not fetch nutrition data");
                return ResponseEntity.status(500).body(response);
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> fullProduct = (Map<String, Object>) barcodeResponse.getBody().get("product");

            if (fullProduct == null) {
                response.put("error", "Product not found");
                return ResponseEntity.status(404).body(response);
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> nutriments = (Map<String, Object>) fullProduct.get("nutriments");

            Map<String, Double> nutritionData = extractNutritionData(nutriments);
            Map<String, Object> mlResult = callMLService(nutritionData);

            response.put("productName", fullProduct.get("product_name"));
            response.put("brand", fullProduct.get("brands"));
            response.put("barcode", barcode);
            response.put("nutrition", nutritionData);
            response.put("recommendation", mlResult);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error: {}", e.getMessage(), e);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // ==================== IMAGE ANALYSIS (YOLO + Open Food Facts) ====================
    @PostMapping("/analyze-image")
    public ResponseEntity<Map<String, Object>> analyzeByImage(@RequestParam("image") MultipartFile image) {
        Map<String, Object> response = new HashMap<>();
        try {
            log.info("📸 Analyzing food image...");

            // 1. Appeler le service Python YOLO pour reconnaître l'aliment
            String foodName = callFoodRecognitionService(image);

            if (foodName == null || foodName.isEmpty()) {
                response.put("error", "Aucun aliment reconnu dans l'image");
                response.put("message", "Veuillez prendre une photo plus claire du produit ou utiliser le scan de code-barres");
                return ResponseEntity.status(404).body(response);
            }

            log.info("🍽️ Food recognized: {}", foodName);

            // 2. Chercher les informations nutritionnelles sur Open Food Facts
            Map<String, Object> productInfo = searchProductByNameSimple(foodName);

            if (productInfo == null || productInfo.isEmpty()) {
                response.put("foodName", foodName);
                response.put("warning", "Produit reconnu mais non trouvé dans la base nutritionnelle");
                response.put("recommendation", Map.of(
                        "message", "⚠️ Analyse impossible - Utilisez le scan de code-barres pour des résultats précis",
                        "is_recommended", false
                ));
                return ResponseEntity.ok(response);
            }

            // 3. Extraire les données nutritionnelles
            Map<String, Double> nutritionData = extractNutritionDataFromMap(productInfo);

            // 4. Appeler le ML model pour la recommandation
            Map<String, Object> mlResult = callMLService(nutritionData);

            response.put("foodName", foodName);
            response.put("productName", productInfo.get("productName"));
            response.put("brand", productInfo.get("brand"));
            response.put("nutrition", nutritionData);
            response.put("recommendation", mlResult);

            return ResponseEntity.ok(response);

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
    private Map<String, Object> searchProductByNameSimple(String productName) {
        Map<String, Object> result = new HashMap<>();
        try {
            String encodedName = URLEncoder.encode(productName, StandardCharsets.UTF_8);
            String url = "https://world.openfoodfacts.org/cgi/search.pl?search_terms=" + encodedName +
                    "&search_simple=1&action=process&json=1&page_size=1";

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "DiacareApp/1.0");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            if (response.getBody() != null) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> products = (List<Map<String, Object>>) response.getBody().get("products");
                if (products != null && !products.isEmpty()) {
                    Map<String, Object> product = products.get(0);
                    result.put("productName", product.get("product_name"));
                    result.put("brand", product.get("brands"));

                    @SuppressWarnings("unchecked")
                    Map<String, Object> nutriments = (Map<String, Object>) product.get("nutriments");
                    if (nutriments != null) {
                        result.put("sugars_100g", getDoubleValue(nutriments, "sugars_100g"));
                        result.put("fat_100g", getDoubleValue(nutriments, "fat_100g"));
                        result.put("carbohydrates_100g", getDoubleValue(nutriments, "carbohydrates_100g"));
                        result.put("proteins_100g", getDoubleValue(nutriments, "proteins_100g"));
                        result.put("fiber_100g", getDoubleValue(nutriments, "fiber_100g"));
                        result.put("sodium_100g", getDoubleValue(nutriments, "sodium_100g"));
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error searching product: {}", e.getMessage());
        }
        return result;
    }

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

    private Map<String, Double> extractNutritionDataFromMap(Map<String, Object> productInfo) {
        Map<String, Double> nutrition = new HashMap<>();
        nutrition.put("sugars_100g", getDoubleFromObject(productInfo.get("sugars_100g")));
        nutrition.put("fat_100g", getDoubleFromObject(productInfo.get("fat_100g")));
        nutrition.put("carbohydrates_100g", getDoubleFromObject(productInfo.get("carbohydrates_100g")));
        nutrition.put("proteins_100g", getDoubleFromObject(productInfo.get("proteins_100g")));
        nutrition.put("fiber_100g", getDoubleFromObject(productInfo.get("fiber_100g")));
        nutrition.put("sodium_100g", getDoubleFromObject(productInfo.get("sodium_100g")));
        return nutrition;
    }

    private double getDoubleValue(Map<String, Object> map, String key) {
        if (map == null) return 0.0;
        Object value = map.get(key);
        if (value == null) return 0.0;
        if (value instanceof Number) return ((Number) value).doubleValue();
        return 0.0;
    }

    private double getDoubleFromObject(Object value) {
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