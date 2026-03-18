package tn.esprit.spring.diacarebackend.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.OneToMany;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.List;

public class Category {
    @Entity
    @Data
    public class Category {

        @Id
        @GeneratedValue
        private Long id;

        private String name;

        @OneToMany(mappedBy = "category")
        private List<Product> products;
    }
}
