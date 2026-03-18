package tn.esprit.spring.diacarebackend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.spring.diacarebackend.entities.Cart;
import tn.esprit.spring.diacarebackend.services.CartService;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public Cart getCart(@RequestHeader("Authorization") String token) {
        return cartService.getCart(token);
    }

    @PostMapping("/add")
    public Cart add(
            @RequestParam Long productId,
            @RequestParam int qty,
            @RequestHeader("Authorization") String token
    ) {
        return cartService.addToCart(productId, qty, token);
    }
}