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

    // ==================== IMAGE ANALYSIS (CORRIGÉE) ====================
    @PostMapping("/analyze-image")
    public ResponseEntity<Map<String, Object>> analyzeByImage(@RequestParam("image") MultipartFile image) {
        Map<String, Object> response = new HashMap<>();
        try {
            log.info("📸 Début analyse image: {}", image.getOriginalFilename());
            log.info("📤 Appel du service YOLO à: {}", foodRecognitionUrl);

            // Appeler le service YOLO avec timeout
            String foodName = callFoodRecognitionServiceWithTimeout(image);

            log.info("🔍 Aliment reconnu par YOLO: '{}'", foodName);

            if (foodName == null || foodName.isEmpty()) {
                log.warn("⚠️ Aucun aliment reconnu dans l'image");
                response.put("error", "Aucun aliment reconnu dans l'image");
                response.put("productName", "aliment_non_reconnu");
                response.put("nutrition", getEmptyNutrition());
                response.put("recommendation", Map.of(
                        "message", "Aucun aliment reconnu - essayez une autre image",
                        "is_recommended", false,
                        "confidence", 0
                ));
                return ResponseEntity.ok(response);
            }

            // Reuse the name search logic (which already handles errors)
            Map<String, String> nameRequest = Map.of("productName", foodName);
            ResponseEntity<Map<String, Object>> nameResult = analyzeByName(nameRequest);

            // Add recognition info to the response
            Map<String, Object> resultBody = nameResult.getBody();
            if (resultBody != null) {
                resultBody.put("recognized_from_image", foodName);
                resultBody.put("analysis_method", "image_recognition");
                resultBody.put("yolo_service_status", "connected");
            }

            log.info("✅ Analyse image terminée avec succès");
            return nameResult;

        } catch (Exception e) {
            log.error("❌ Erreur analyse image: {}", e.getMessage(), e);
            response.put("error", "Erreur lors de l'analyse de l'image: " + e.getMessage());
            response.put("productName", "erreur_analyse");
            response.put("nutrition", getEmptyNutrition());
            response.put("recommendation", Map.of(
                    "message", "Service de reconnaissance temporairement indisponible",
                    "is_recommended", false,
                    "confidence", 0
            ));
            return ResponseEntity.status(500).body(response);
        }
    }

    // ==================== FOOD RECOGNITION SERVICE (YOLO) AVEC TIMEOUT ====================
    private String callFoodRecognitionServiceWithTimeout(MultipartFile image) throws Exception {
        String url = foodRecognitionUrl + "/recognize";

        // Créer un RestTemplate avec timeout
        RestTemplate customRestTemplate = new RestTemplate();
        org.springframework.http.client.SimpleClientHttpRequestFactory factory =
                new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000); // 5 secondes de connexion
        factory.setReadTimeout(10000);   // 10 secondes de lecture
        customRestTemplate.setRequestFactory(factory);

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

        log.info("📤 Envoi de l'image à YOLO: {}", image.getOriginalFilename());

        try {
            ResponseEntity<Map> response = customRestTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);

            log.info("📥 Réponse YOLO reçue - Status: {}", response.getStatusCode());
            log.info("📥 Corps de réponse: {}", response.getBody());

            if (response.getBody() != null) {
                // Vérifier différents formats de réponse possibles
                if (response.getBody().containsKey("success") && Boolean.TRUE.equals(response.getBody().get("success"))) {
                    String foodName = (String) response.getBody().get("food_name");
                    if (foodName != null && !foodName.isEmpty()) {
                        return foodName;
                    }
                }

                // Essayer d'autres formats
                if (response.getBody().containsKey("food_name")) {
                    String foodName = (String) response.getBody().get("food_name");
                    if (foodName != null && !foodName.isEmpty()) {
                        return foodName;
                    }
                }

                if (response.getBody().containsKey("detected_food")) {
                    String foodName = (String) response.getBody().get("detected_food");
                    if (foodName != null && !foodName.isEmpty()) {
                        return foodName;
                    }
                }

                // Si la réponse contient une liste d'aliments détectés
                if (response.getBody().containsKey("detected_foods")) {
                    @SuppressWarnings("unchecked")
                    List<String> detectedFoods = (List<String>) response.getBody().get("detected_foods");
                    if (detectedFoods != null && !detectedFoods.isEmpty()) {
                        return detectedFoods.get(0);
                    }
                }
            }

            log.warn("⚠️ Format de réponse YOLO non reconnu");
            return null;

        } catch (Exception e) {
            log.error("❌ Erreur appel YOLO: {}", e.getMessage());
            throw new Exception("YOLO service error: " + e.getMessage());
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
        response.put("yolo_service_url", foodRecognitionUrl);
        response.put("ml_service_url", mlServiceUrl);
        return ResponseEntity.ok(response);
    }

    // Endpoint pour tester YOLO directement
    // Endpoint pour tester YOLO directement
    @GetMapping("/test-yolo")
    public ResponseEntity<Map<String, Object>> testYoloService() {
        Map<String, Object> response = new HashMap<>();
        try {
            String url = foodRecognitionUrl + "/";
            ResponseEntity<String> testResponse = restTemplate.getForEntity(url, String.class);

            response.put("yolo_service_status", "UP");
            response.put("url", foodRecognitionUrl);
            response.put("status_code", testResponse.getStatusCode().value()); // ✅ CORRECTION ICI
            response.put("response_preview", testResponse.getBody());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("yolo_service_status", "DOWN");
            response.put("url", foodRecognitionUrl);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }

    }
}