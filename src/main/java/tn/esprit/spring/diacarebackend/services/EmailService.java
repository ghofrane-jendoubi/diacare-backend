package tn.esprit.spring.diacarebackend.services;

public interface EmailService {
    void sendOrderEmail(String email, Long orderId, double total);
    void sendOrderConfirmationEmail(String email, Long orderId);
}