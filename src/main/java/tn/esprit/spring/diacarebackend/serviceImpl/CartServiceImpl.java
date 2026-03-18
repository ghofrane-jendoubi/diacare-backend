package tn.esprit.spring.diacarebackend.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.spring.diacarebackend.entities.Cart;
import tn.esprit.spring.diacarebackend.entities.CartItem;
import tn.esprit.spring.diacarebackend.entities.Product;
import tn.esprit.spring.diacarebackend.entities.User;
import tn.esprit.spring.diacarebackend.repository.CartRepository;
import tn.esprit.spring.diacarebackend.repository.ProductRepository;
import tn.esprit.spring.diacarebackend.services.CartService;
import tn.esprit.spring.diacarebackend.services.UserService;


@Service
    @RequiredArgsConstructor
    public class CartServiceImpl implements CartService {

        private final CartRepository cartRepository;
        private final ProductRepository productRepository;
        private final UserService userService;

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
                    .orElseThrow();

            CartItem item = new CartItem();
            item.setProduct(product);
            item.setQuantity(qty);
            item.setCart(cart);

            cart.getItems().add(item);

            double total = cart.getItems().stream()
                    .mapToDouble(i -> i.getProduct().getPrice() * i.getQuantity())
                    .sum();

            cart.setTotalPrice(total);

            return cartRepository.save(cart);
        }
    }
