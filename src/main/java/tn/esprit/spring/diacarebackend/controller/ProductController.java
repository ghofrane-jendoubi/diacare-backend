package tn.esprit.spring.diacarebackend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.spring.diacarebackend.entities.Product;
import tn.esprit.spring.diacarebackend.entities.ProductType;
import tn.esprit.spring.diacarebackend.repository.ProductRepository;
import tn.esprit.spring.diacarebackend.services.ProductService;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductRepository productRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    private final String UPLOAD_DIR = System.getProperty("user.dir") + File.separator + "uploads" + File.separator;

    @Value("${youtube.api.key:AIzaSyDyQh-_NjpckuimH4AjAvy89EUlFRjrQhk}")
    private String youtubeApiKey;

    // ==================== CRUD NORMAL ====================

    @PostMapping
    public ResponseEntity<?> add(@RequestBody Product product) {
        try {
            Product savedProduct = productService.addProduct(product);
            log.info("Product added successfully: {}", savedProduct.getId());
            return ResponseEntity.ok(savedProduct);
        } catch (Exception e) {
            log.error("Error adding product: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public List<Product> getAll() {
        log.info("Fetching all products");
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getOne(@PathVariable Long id) {
        try {
            Product product = productService.getProduct(id);
            return ResponseEntity.ok(product);
        } catch (Exception e) {
            log.error("Product not found with id: {}", id, e);
            return ResponseEntity.notFound().build();
        }
    }

    // ==================== YOUTUBE VIDEOS ENDPOINT ====================

    @GetMapping("/{id}/videos")
    public ResponseEntity<Map<String, Object>> getProductVideos(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Product product = productService.getProduct(id);

            if (product == null) {
                response.put("error", "Product not found");
                return ResponseEntity.status(404).body(response);
            }

            // Ne chercher des vidéos que pour les produits médicaux
            if (product.getType() == ProductType.MEDICAL) {
                String searchQuery = URLEncoder.encode(product.getName() + " tutoriel utilisation", StandardCharsets.UTF_8);
                String url = "https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=4&type=video&q=" + searchQuery + "&key=" + youtubeApiKey;

                ResponseEntity<Map> apiResponse = restTemplate.getForEntity(url, Map.class);

                if (apiResponse.getBody() != null) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> items = (List<Map<String, Object>>) apiResponse.getBody().get("items");

                    List<Map<String, Object>> videos = new ArrayList<>();
                    if (items != null) {
                        for (Map<String, Object> item : items) {
                            Map<String, Object> video = new HashMap<>();
                            @SuppressWarnings("unchecked")
                            Map<String, Object> snippet = (Map<String, Object>) item.get("snippet");
                            @SuppressWarnings("unchecked")
                            Map<String, Object> idMap = (Map<String, Object>) item.get("id");
                            @SuppressWarnings("unchecked")
                            Map<String, Object> thumbnails = (Map<String, Object>) snippet.get("thumbnails");
                            @SuppressWarnings("unchecked")
                            Map<String, Object> mediumThumb = (Map<String, Object>) thumbnails.get("medium");

                            video.put("videoId", idMap.get("videoId"));
                            video.put("title", snippet.get("title"));
                            video.put("description", snippet.get("description"));
                            video.put("thumbnailUrl", mediumThumb != null ? mediumThumb.get("url") : null);
                            videos.add(video);
                        }
                    }
                    response.put("videos", videos);
                    response.put("productName", product.getName());
                    response.put("productType", "MEDICAL");
                } else {
                    response.put("videos", new ArrayList<>());
                    response.put("message", "No videos found");
                }
            } else {
                response.put("videos", new ArrayList<>());
                response.put("message", "Videos only available for medical products");
                response.put("productType", "ALIMENTAIRE");
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error fetching YouTube videos: {}", e.getMessage(), e);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // ==================== UPDATE PRODUCT ====================

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        try {
            product.setId(id);
            Product updatedProduct = productService.updateProduct(product);
            log.info("Product updated successfully: {}", id);
            return ResponseEntity.ok(updatedProduct);
        } catch (Exception e) {
            log.error("Error updating product {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            log.info("Product deleted successfully: {}", id);
            return ResponseEntity.ok(Map.of("message", "Product deleted successfully"));
        } catch (Exception e) {
            log.error("Error deleting product {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== UPLOAD WITH IMAGE ====================

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addProductWithImage(
            @RequestParam("name") String name,
            @RequestParam("price") double price,
            @RequestParam("type") String type,
            @RequestParam(value = "barcode", required = false) String barcode,
            @RequestParam(value = "stock", required = false) Integer stock,
            @RequestParam(value = "sugarLevel", required = false) Double sugarLevel,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("image") MultipartFile file
    ) {
        try {
            if (file.isEmpty()) {
                log.warn("Empty file received");
                return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                log.warn("Invalid file type: {}", contentType);
                return ResponseEntity.badRequest().body(Map.of("error", "Only image files are allowed"));
            }

            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID() + fileExtension;

            Path filePath = Paths.get(UPLOAD_DIR + fileName);
            Files.copy(file.getInputStream(), filePath);
            log.info("File saved at: {}", filePath.toString());

            Product product = new Product();
            product.setName(name);
            product.setPrice(price);
            product.setType(ProductType.valueOf(type));
            product.setImage(fileName);

            if (barcode != null && !barcode.isEmpty()) product.setBarcode(barcode);
            if (stock != null) product.setStock(stock);
            if (sugarLevel != null) product.setSugarLevel(sugarLevel);
            if (description != null && !description.isEmpty()) product.setDescription(description);

            Product savedProduct = productRepository.save(product);
            log.info("Product saved with ID: {}", savedProduct.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("id", savedProduct.getId());
            response.put("name", savedProduct.getName());
            response.put("price", savedProduct.getPrice());
            response.put("type", savedProduct.getType());
            response.put("image", savedProduct.getImage());
            response.put("imageUrl", "http://localhost:8080/api/products/images/" + fileName);
            response.put("message", "Product added successfully");

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            log.error("IO error during file upload: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to upload image: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during product upload: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to save product: " + e.getMessage()));
        }
    }

    // ==================== GET IMAGE ====================

    @GetMapping("/images/{filename}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
        try {
            Path path = Paths.get(UPLOAD_DIR + filename);
            Resource resource = new UrlResource(path.toUri());

            if (resource.exists() && resource.isReadable()) {
                String contentType = Files.probeContentType(path);
                if (contentType == null) {
                    contentType = "image/jpeg";
                }
                log.debug("Serving image: {}", filename);
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .body(resource);
            } else {
                log.warn("Image not found: {}", filename);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error serving image {}: {}", filename, e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/images/{filename}")
    public ResponseEntity<?> deleteImage(@PathVariable String filename) {
        try {
            Path path = Paths.get(UPLOAD_DIR + filename);
            boolean deleted = Files.deleteIfExists(path);
            if (deleted) {
                log.info("Image deleted: {}", filename);
                return ResponseEntity.ok(Map.of("message", "Image deleted successfully"));
            } else {
                log.warn("Image not found for deletion: {}", filename);
                return ResponseEntity.notFound().build();
            }
        } catch (IOException e) {
            log.error("Error deleting image {}: {}", filename, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete image: " + e.getMessage()));
        }
    }
    // ==================== UPDATE PRODUCT WITH IMAGE ====================

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProductWithImage(
            @PathVariable Long id,
            @RequestParam("name") String name,
            @RequestParam("price") double price,
            @RequestParam("type") String type,
            @RequestParam(value = "barcode", required = false) String barcode,
            @RequestParam(value = "stock", required = false) Integer stock,
            @RequestParam(value = "sugarLevel", required = false) Double sugarLevel,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "image", required = false) MultipartFile file
    ) {
        try {
            log.info("Updating product {} with image", id);

            // Récupérer le produit existant
            Product existingProduct = productService.getProduct(id);
            if (existingProduct == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Product not found with id: " + id));
            }

            // Mettre à jour les champs
            existingProduct.setName(name);
            existingProduct.setPrice(price);
            existingProduct.setType(ProductType.valueOf(type));

            if (barcode != null && !barcode.isEmpty()) existingProduct.setBarcode(barcode);
            if (stock != null) existingProduct.setStock(stock);
            if (sugarLevel != null) existingProduct.setSugarLevel(sugarLevel);
            if (description != null && !description.isEmpty()) existingProduct.setDescription(description);

            // Si une nouvelle image est fournie
            if (file != null && !file.isEmpty()) {
                // Supprimer l'ancienne image
                if (existingProduct.getImage() != null && !existingProduct.getImage().isEmpty()) {
                    Path oldImagePath = Paths.get(UPLOAD_DIR + existingProduct.getImage());
                    Files.deleteIfExists(oldImagePath);
                }

                // Sauvegarder la nouvelle image
                String contentType = file.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Only image files are allowed"));
                }

                String originalFilename = file.getOriginalFilename();
                String fileExtension = "";
                if (originalFilename != null && originalFilename.contains(".")) {
                    fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }
                String fileName = UUID.randomUUID() + fileExtension;

                Path filePath = Paths.get(UPLOAD_DIR + fileName);
                Files.copy(file.getInputStream(), filePath);

                existingProduct.setImage(fileName);
                log.info("New image saved: {}", fileName);
            }

            Product updatedProduct = productRepository.save(existingProduct);
            log.info("Product updated successfully: {}", id);

            Map<String, Object> response = new HashMap<>();
            response.put("id", updatedProduct.getId());
            response.put("name", updatedProduct.getName());
            response.put("price", updatedProduct.getPrice());
            response.put("type", updatedProduct.getType());
            response.put("image", updatedProduct.getImage());
            response.put("barcode", updatedProduct.getBarcode());
            response.put("stock", updatedProduct.getStock());
            response.put("sugarLevel", updatedProduct.getSugarLevel());
            response.put("description", updatedProduct.getDescription());
            response.put("message", "Product updated successfully");

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            log.error("IO error during file upload: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to upload image: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating product {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}