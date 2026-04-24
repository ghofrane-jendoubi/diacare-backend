package tn.esprit.spring.diacarebackend.serviceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.spring.diacarebackend.entities.Cart;
import tn.esprit.spring.diacarebackend.entities.CartItem;
import tn.esprit.spring.diacarebackend.entities.Product;
import tn.esprit.spring.diacarebackend.entities.User;
import tn.esprit.spring.diacarebackend.repository.CartItemRepository;
import tn.esprit.spring.diacarebackend.repository.CartRepository;
import tn.esprit.spring.diacarebackend.repository.ProductRepository;
import tn.esprit.spring.diacarebackend.repository.UserRepository;
import tn.esprit.spring.diacarebackend.services.CartService;

import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public Cart getCartByPatientId(Long patientId) {
        log.info("Getting cart for patient: {}", patientId);

        User user = userRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + patientId));

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

        return cart;
    }

    @Override
    @Transactional
    public Cart addToCart(Long patientId, Long productId, int qty) {
        log.info("Adding product {} to cart for patient {}", productId, patientId);

        Cart cart = getCartByPatientId(patientId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        // Check if product already exists in cart
        CartItem existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + qty);
            cartItemRepository.save(existingItem);
        } else {
            CartItem newItem = new CartItem();
            newItem.setProduct(product);
            newItem.setQuantity(qty);
            newItem.setPrice(product.getPrice());  // ✅ Stocker le prix au moment de l'ajout
            newItem.setCart(cart);
            cart.getItems().add(newItem);
            cartItemRepository.save(newItem);
        }

        updateTotal(cart);
        return cartRepository.save(cart);
    }

    @Override
    @Transactional
    public Cart updateItem(Long patientId, Long itemId, int qty) {
        log.info("Updating cart item {} to quantity {} for patient {}", itemId, qty, patientId);

        Cart cart = getCartByPatientId(patientId);

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found with id: " + itemId));

        // Verify item belongs to this cart
        if (!item.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("Item does not belong to this cart");
        }

        if (qty <= 0) {
            cart.getItems().remove(item);
            cartItemRepository.delete(item);
        } else {
            item.setQuantity(qty);
            cartItemRepository.save(item);
        }

        updateTotal(cart);
        return cartRepository.save(cart);
    }

    @Override
    public Cart save(Cart cart) {
        return cartRepository.save(cart);
    }

    @Override
    @Transactional
    public void removeItem(Long patientId, Long itemId) {
        log.info("Removing cart item {} for patient {}", itemId, patientId);

        Cart cart = getCartByPatientId(patientId);

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found with id: " + itemId));

        // Verify item belongs to this cart
        if (!item.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("Item does not belong to this cart");
        }

        cart.getItems().remove(item);
        cartItemRepository.delete(item);

        updateTotal(cart);
        cartRepository.save(cart);
    }

    private void updateTotal(Cart cart) {
        double total = cart.getItems().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
        cart.setTotalPrice(total);
    }
}