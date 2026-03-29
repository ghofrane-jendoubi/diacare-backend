package tn.esprit.spring.diacarebackend.services;

import tn.esprit.spring.diacarebackend.entities.Cart;

public interface CartService {

    Cart getCart(String token);

    Cart addToCart(Long productId, int qty, String token);
    Cart updateItem(Long itemId, int qty, String token);
    Cart save(Cart cart);
    void removeItem(Long itemId, String token);
}