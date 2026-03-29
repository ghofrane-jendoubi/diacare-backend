package tn.esprit.spring.diacarebackend.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.spring.diacarebackend.entities.Cart;
import tn.esprit.spring.diacarebackend.entities.CartItem;
import tn.esprit.spring.diacarebackend.entities.Product;
import tn.esprit.spring.diacarebackend.entities.User;
import tn.esprit.spring.diacarebackend.repository.CartRepository;
import tn.esprit.spring.diacarebackend.repository.ProductRepository;
import tn.esprit.spring.diacarebackend.repository.CartItemRepository;
import tn.esprit.spring.diacarebackend.services.CartService;
import tn.esprit.spring.diacarebackend.services.UserService;


@Service
    @RequiredArgsConstructor
    public class CartServiceImpl implements CartService {

        private final CartRepository cartRepository;
        private final ProductRepository productRepository;
        private final UserService userService;
    private final CartItemRepository cartItemRepository;
    @Override
    public Cart getCart(String token) {
        User user = userService.getCurrentUser(token);
        return cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart cart = new Cart();
                    cart.setUser(user);
                    return cartRepository.save(cart);
                });
    }

    @Override
    public Cart addToCart(Long productId, int qty, String token) {
        Cart cart = getCart(token);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Check if product already exists in cart
        CartItem existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + qty);
        } else {
            CartItem newItem = new CartItem();
            newItem.setProduct(product);
            newItem.setQuantity(qty);
            newItem.setCart(cart);
            cart.getItems().add(newItem);
        }

        // Update total price
        double total = cart.getItems().stream()
                .mapToDouble(i -> i.getProduct().getPrice() * i.getQuantity())
                .sum();
        cart.setTotalPrice(total);

        return cartRepository.save(cart);
    }
    @Override
    public Cart save(Cart cart) {
        return cartRepository.save(cart);
    }
    @Override
    public Cart updateItem(Long itemId, int qty, String token) {
        Cart cart = getCart(token);

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Item not found"));

        item.setQuantity(qty);

        updateTotal(cart);
        return cartRepository.save(cart);
    }

    @Override
    public void removeItem(Long itemId, String token) {
        Cart cart = getCart(token);

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Item not found"));

        cart.getItems().remove(item);

        // 🔥 IMPORTANT: DELETE FROM DB
        cartItemRepository.delete(item);
        updateTotal(cart);

        cartRepository.save(cart);
    }

    private void updateTotal(Cart cart) {
        double total = cart.getItems().stream()
                .mapToDouble(i -> i.getProduct().getPrice() * i.getQuantity())
                .sum();
        cart.setTotalPrice(total);
    }}