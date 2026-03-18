package tn.esprit.spring.diacarebackend.services;


import tn.esprit.spring.diacarebackend.entities.Product;

import java.util.List;

public interface ProductService {

        Product addProduct(Product product);

        List<Product> getAllProducts();

        Product getProduct(Long id);

}
