package tn.esprit.spring.diacarebackend.services;

import tn.esprit.spring.diacarebackend.entities.Cart;

public interface CartService {

    Cart getCartByPatientId(Long patientId);

    Cart addToCart(Long patientId, Long productId, int qty);

    Cart updateItem(Long patientId, Long itemId, int qty);

    Cart save(Cart cart);

    void removeItem(Long patientId, Long itemId);
}