package tn.esprit.spring.diacarebackend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import tn.esprit.spring.diacarebackend.entities.Cart;
import tn.esprit.spring.diacarebackend.entities.CartItem;
import tn.esprit.spring.diacarebackend.entities.Product;
import tn.esprit.spring.diacarebackend.entities.User;
import tn.esprit.spring.diacarebackend.repository.CartItemRepository;
import tn.esprit.spring.diacarebackend.repository.CartRepository;
import tn.esprit.spring.diacarebackend.repository.ProductRepository;
import tn.esprit.spring.diacarebackend.repository.UserRepository;

import java.util.ArrayList;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;

    @GetMapping
    @Transactional
    public ResponseEntity<?> getCart(@RequestParam Long patientId) {
        try {
            log.info("=== GET CART ===");
            log.info("Patient ID: {}", patientId);

            User user = userRepository.findById(patientId)
                    .orElse(null);

            if (user == null) {
                log.error("User not found with id: {}", patientId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User not found with id: " + patientId));
            }

            Cart cart = cartRepository.findByUser(user)
                    .orElseGet(() -> {
                        Cart newCart = new Cart();
                        newCart.setUser(user);
                        newCart.setTotalPrice(0.0);
                        newCart.setItems(new ArrayList<>());
                        return cartRepository.save(newCart);
                    });

            // ✅ Force l'initialisation de la collection
            cart.getItems().size();

            log.info("Cart found/created with id: {}", cart.getId());
            return ResponseEntity.ok(cart);

        } catch (Exception e) {
            log.error("Error in getCart: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/add")
    @Transactional
    public ResponseEntity<?> addToCart(
            @RequestParam Long productId,
            @RequestParam int qty,
            @RequestParam Long patientId) {
        try {
            log.info("=== ADD TO CART ===");
            log.info("Product ID: {}, Quantity: {}, Patient ID: {}", productId, qty, patientId);

            User user = userRepository.findById(patientId)
                    .orElse(null);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User not found"));
            }

            Product product = productRepository.findById(productId)
                    .orElse(null);
            if (product == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Product not found"));
            }

            Cart cart = cartRepository.findByUser(user)
                    .orElseGet(() -> {
                        Cart newCart = new Cart();
                        newCart.setUser(user);
                        newCart.setTotalPrice(0.0);
                        newCart.setItems(new ArrayList<>());
                        return cartRepository.save(newCart);
                    });

            // ✅ Force l'initialisation de la collection
            cart.getItems().size();

            // Vérifier si le produit est déjà dans le panier
            CartItem existingItem = null;
            for (CartItem item : cart.getItems()) {
                if (item.getProduct().getId().equals(productId)) {
                    existingItem = item;
                    break;
                }
            }

            if (existingItem != null) {
                existingItem.setQuantity(existingItem.getQuantity() + qty);
                cartItemRepository.save(existingItem);
            } else {
                CartItem newItem = new CartItem();
                newItem.setProduct(product);
                newItem.setQuantity(qty);
                newItem.setPrice(product.getPrice());
                newItem.setCart(cart);
                cart.getItems().add(newItem);
                cartItemRepository.save(newItem);
            }

            // Mettre à jour le total
            double total = 0.0;
            for (CartItem item : cart.getItems()) {
                total += item.getPrice() * item.getQuantity();
            }
            cart.setTotalPrice(total);
            cartRepository.save(cart);

            return ResponseEntity.ok(cart);

        } catch (Exception e) {
            log.error("Error in addToCart: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/items/{itemId}")
    @Transactional
    public ResponseEntity<?> updateItem(
            @PathVariable Long itemId,
            @RequestBody Map<String, Integer> body,
            @RequestParam Long patientId) {
        try {
            int qty = body.get("quantity");

            User user = userRepository.findById(patientId).orElse(null);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User not found"));
            }

            Cart cart = cartRepository.findByUser(user).orElse(null);
            if (cart == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Cart not found"));
            }

            CartItem item = cartItemRepository.findById(itemId).orElse(null);
            if (item == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Item not found"));
            }

            if (qty <= 0) {
                cart.getItems().remove(item);
                cartItemRepository.delete(item);
            } else {
                item.setQuantity(qty);
                cartItemRepository.save(item);
            }

            double total = 0.0;
            for (CartItem cartItem : cart.getItems()) {
                total += cartItem.getPrice() * cartItem.getQuantity();
            }
            cart.setTotalPrice(total);
            cartRepository.save(cart);

            return ResponseEntity.ok(cart);

        } catch (Exception e) {
            log.error("Error in updateItem: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/items/{itemId}")
    @Transactional
    public ResponseEntity<?> removeItem(
            @PathVariable Long itemId,
            @RequestParam Long patientId) {
        try {
            User user = userRepository.findById(patientId).orElse(null);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User not found"));
            }

            Cart cart = cartRepository.findByUser(user).orElse(null);
            if (cart == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Cart not found"));
            }

            CartItem item = cartItemRepository.findById(itemId).orElse(null);
            if (item == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Item not found"));
            }

            cart.getItems().remove(item);
            cartItemRepository.delete(item);

            double total = 0.0;
            for (CartItem cartItem : cart.getItems()) {
                total += cartItem.getPrice() * cartItem.getQuantity();
            }
            cart.setTotalPrice(total);
            cartRepository.save(cart);

            return ResponseEntity.ok(Map.of("message", "Item removed successfully"));

        } catch (Exception e) {
            log.error("Error in removeItem: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}