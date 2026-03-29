package tn.esprit.spring.diacarebackend.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.spring.diacarebackend.entities.Cart;
import tn.esprit.spring.diacarebackend.services.CartService;

import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public Cart getCart(@RequestHeader(value = "Authorization", required = false) String token) {
        return cartService.getCart(token);
    }

    @PostMapping("/add")
    public Cart add(@RequestParam Long productId,
                    @RequestParam int qty,
                    @RequestHeader(value = "Authorization", required = false) String token) {
        return cartService.addToCart(productId, qty, token);
    }
    @PutMapping("/items/{itemId}")
    public Cart updateItem(
            @PathVariable Long itemId,
            @RequestBody Map<String, Integer> body,
            @RequestHeader(value = "Authorization", required = false) String token
    ) {
        int qty = body.get("quantity");
        return cartService.updateItem(itemId, qty, token);
    }

    @DeleteMapping("/items/{itemId}")
    public void removeItem(@PathVariable Long itemId,
                           @RequestHeader(value = "Authorization", required = false) String token) {
        cartService.removeItem(itemId, token);
    }
}