package tn.esprit.spring.diacarebackend.serviceImpl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import tn.esprit.spring.diacarebackend.services.EmailService;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendOrderEmail(String email, Long orderId, double total) {
        // Simple email en texte (peut être utilisé si nécessaire)
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(email);
            helper.setSubject("Confirmation de commande");
            helper.setText("Votre commande n°" + orderId + " d'un montant de " + total + " DT a été confirmée.");
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Erreur lors de l'envoi de l'email", e);
        }
    }

    @Override
    public void sendOrderConfirmationEmail(String email, Long orderId) {
        String confirmUrl = "http://localhost:4200/patient/paiement?action=confirm&orderId=" + orderId;
        String cancelUrl = "http://localhost:4200/patient/paiement?action=cancel&orderId=" + orderId;
        String htmlContent = "<!DOCTYPE html>" +
                "<html>" +
                "<body style='font-family: Arial, sans-serif; text-align: center;'>" +
                "<h2>Confirmation de commande</h2>" +
                "<p>Votre commande a bien été enregistrée.</p>" +
                "<a href='" + confirmUrl + "' " +
                "style='display: inline-block; padding: 10px 20px; margin: 10px; background-color: #28a745; color: white; text-decoration: none; border-radius: 5px;'>" +
                "Oui, confirmer" +
                "</a>" +
                "<a href='" + cancelUrl + "' " +
                "style='display: inline-block; padding: 10px 20px; margin: 10px; background-color: #dc3545; color: white; text-decoration: none; border-radius: 5px;'>" +
                "Non, annuler" +
                "</a>" +
                "<p>Si vous n'avez pas effectué cette commande, ignorez cet email.</p>" +
                "</body>" +
                "</html>";
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(email);
            helper.setSubject("Confirmation de commande");
            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Erreur lors de l'envoi de l'email de confirmation", e);
        }

    }
    }
