package tn.esprit.spring.diacarebackend.entities;

import jakarta.persistence.*;
import lombok.Data;


    @Entity
    @Data
    public class Product {

        @Id
        @GeneratedValue
        private Long id;

        private String name;
        private String description;
        private double price;
        private int stock;

        private String image;
        private String barcode;

        @Enumerated(EnumType.STRING)
        private ProductType type; // MEDICAL / ALIMENTAIRE

        private double sugarLevel;

        @ManyToOne
        private Category category;
    }


