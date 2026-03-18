package tn.esprit.spring.diacarebackend.entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Product product;

    private int quantity;

    public void setProduct(Product product) { this.product = product;}
    @ManyToOne
    private Cart cart;
}