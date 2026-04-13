package tn.esprit.spring.diacarebackend.serviceImpl;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NutritionAPIService {

    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, Object> searchProductByBarcode(String barcode) throws Exception {
        String url = "https://world.openfoodfacts.org/api/v0/product/" + barcode + ".json";

        System.out.println("📡 Calling Open Food Facts API: " + url);

        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            Map<String, Object> product = (Map<String, Object>) response.getBody().get("product");

            if (product == null) {
                throw new Exception("Product not found for barcode: " + barcode);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("productName", product.get("product_name"));
            result.put("brand", product.get("brands"));
            result.put("ingredients", product.get("ingredients_text"));

            Map<String, Object> nutriments = (Map<String, Object>) product.get("nutriments");
            if (nutriments != null) {
                result.put("fat", getDouble(nutriments.get("fat_100g")));
                result.put("saturatedFat", getDouble(nutriments.get("saturated-fat_100g")));
                result.put("carbohydrates", getDouble(nutriments.get("carbohydrates_100g")));
                result.put("sugars", getDouble(nutriments.get("sugars_100g")));
                result.put("fiber", getDouble(nutriments.get("fiber_100g")));
                result.put("proteins", getDouble(nutriments.get("proteins_100g")));
                result.put("sodium", getDouble(nutriments.get("sodium_100g")));
            }

            System.out.println("✅ Product found: " + result.get("productName"));
            return result;

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            throw new Exception("Failed to fetch product: " + e.getMessage());
        }
    }

    public Map<String, Object> searchProductByName(String productName) throws Exception {
        String encodedName = java.net.URLEncoder.encode(productName, "UTF-8");
        String url = "https://world.openfoodfacts.org/cgi/search.pl?search_terms=" + encodedName +
                "&search_simple=1&action=process&json=1&page_size=1";

        System.out.println("📡 Searching: " + url);

        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            List<Map<String, Object>> products = (List<Map<String, Object>>) response.getBody().get("products");

            if (products == null || products.isEmpty()) {
                throw new Exception("Product not found: " + productName);
            }

            Map<String, Object> product = products.get(0);
            Map<String, Object> result = new HashMap<>();
            result.put("productName", product.get("product_name"));
            result.put("brand", product.get("brands"));
            result.put("ingredients", product.get("ingredients_text"));

            Map<String, Object> nutriments = (Map<String, Object>) product.get("nutriments");
            if (nutriments != null) {
                result.put("fat", getDouble(nutriments.get("fat_100g")));
                result.put("saturatedFat", getDouble(nutriments.get("saturated-fat_100g")));
                result.put("carbohydrates", getDouble(nutriments.get("carbohydrates_100g")));
                result.put("sugars", getDouble(nutriments.get("sugars_100g")));
                result.put("fiber", getDouble(nutriments.get("fiber_100g")));
                result.put("proteins", getDouble(nutriments.get("proteins_100g")));
                result.put("sodium", getDouble(nutriments.get("sodium_100g")));
            }

            System.out.println("✅ Found: " + result.get("productName"));
            return result;

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            throw new Exception("Failed to search product: " + e.getMessage());
        }
    }

    public Map<String, Object> identifyFoodFromImage(MultipartFile image) throws Exception {
        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("foodName", "Product from image");
        mockResponse.put("ingredients", "Please use barcode or name search for accurate results");
        mockResponse.put("fat", 10.0);
        mockResponse.put("saturatedFat", 5.0);
        mockResponse.put("carbohydrates", 20.0);
        mockResponse.put("sugars", 10.0);
        mockResponse.put("fiber", 2.0);
        mockResponse.put("proteins", 5.0);
        mockResponse.put("sodium", 100.0);
        return mockResponse;
    }

    private Double getDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Number) return ((Number) value).doubleValue();
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        return 0.0;
    }
}