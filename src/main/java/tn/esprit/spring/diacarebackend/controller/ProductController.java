package tn.esprit.spring.diacarebackend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.spring.diacarebackend.entities.Product;
import tn.esprit.spring.diacarebackend.entities.ProductType;
import tn.esprit.spring.diacarebackend.repository.ProductRepository;
import tn.esprit.spring.diacarebackend.services.ProductService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductRepository productRepository;

    private final String UPLOAD_DIR = System.getProperty("user.dir") + File.separator + "uploads" + File.separator;

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

    @PutMapping(value = "/upload/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
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
            Product product = productService.getProduct(id);

            product.setName(name);
            product.setPrice(price);
            product.setType(ProductType.valueOf(type));

            if (barcode != null) product.setBarcode(barcode);
            if (stock != null) product.setStock(stock);
            if (sugarLevel != null) product.setSugarLevel(sugarLevel);
            if (description != null) product.setDescription(description);

            if (file != null && !file.isEmpty()) {
                String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                Path filePath = Paths.get(UPLOAD_DIR + fileName);
                Files.copy(file.getInputStream(), filePath);

                product.setImage(fileName);
            }

            return ResponseEntity.ok(productRepository.save(product));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
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
            // Validate file
            if (file.isEmpty()) {
                log.warn("Empty file received");
                return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
            }

            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                log.warn("Invalid file type: {}", contentType);
                return ResponseEntity.badRequest().body(Map.of("error", "Only image files are allowed"));
            }

            // Create upload directory if it doesn't exist
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                boolean created = uploadDir.mkdirs();
                if (created) {
                    log.info("Upload directory created at: {}", UPLOAD_DIR);
                } else {
                    log.error("Failed to create upload directory at: {}", UPLOAD_DIR);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(Map.of("error", "Failed to create upload directory"));
                }
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID() + fileExtension;

            // Save file
            Path filePath = Paths.get(UPLOAD_DIR + fileName);
            Files.copy(file.getInputStream(), filePath);
            log.info("File saved at: {}", filePath.toString());

            // Create product
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
}