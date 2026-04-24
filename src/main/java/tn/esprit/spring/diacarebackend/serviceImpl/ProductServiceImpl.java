package tn.esprit.spring.diacarebackend.serviceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.esprit.spring.diacarebackend.entities.Product;
import tn.esprit.spring.diacarebackend.repository.ProductRepository;
import tn.esprit.spring.diacarebackend.services.ProductService;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public Product addProduct(Product product) {
        log.info("Adding product: {}", product.getName());
        return productRepository.save(product);
    }

    @Override
    public Product updateProduct(Product product) {
        log.info("Updating product with id: {}", product.getId());
        if (!productRepository.existsById(product.getId())) {
            log.error("Product not found with id: {}", product.getId());
            throw new RuntimeException("Product not found");
        }
        return productRepository.save(product);
    }

    @Override
    public void deleteProduct(Long id) {
        log.info("Deleting product with id: {}", id);
        if (!productRepository.existsById(id)) {
            log.error("Product not found with id: {}", id);
            throw new RuntimeException("Product not found");
        }
        productRepository.deleteById(id);
    }

    @Override
    public List<Product> getAllProducts() {
        log.info("Fetching all products");
        return productRepository.findAll();
    }

    @Override
    public Product getProduct(Long id) {
        log.info("Fetching product with id: {}", id);
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }
}