package tn.esprit.spring.diacarebackend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.spring.diacarebackend.entities.Product;
import tn.esprit.spring.diacarebackend.services.ProductService;

import java.util.List;
@RestController
    @RequestMapping("/api/products")
    @RequiredArgsConstructor
    public class ProductController {

        private final ProductService productService;

        @PostMapping
        public Product add(@RequestBody Product product) {
            return productService.addProduct(product);
        }

        @GetMapping
        public List<Product> getAll() {
            return productService.getAllProducts();
        }

        @GetMapping("/{id}")
        public Product getOne(@PathVariable Long id) {
            return productService.getProduct(id);
        }
    }

