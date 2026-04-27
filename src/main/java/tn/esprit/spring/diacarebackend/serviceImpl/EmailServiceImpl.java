package tn.esprit.spring.diacarebackend.serviceImpl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import tn.esprit.spring.diacarebackend.services.EmailService;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendOrderEmail(String email, Long orderId, double total) {
        log.info("Envoi d'email de commande à: {}", email);

        // L'URL de confirmation et d'annulation
        String confirmUrl = "http://localhost:4200/patient/paiement?action=confirm&orderId=" + orderId;
        String cancelUrl = "http://localhost:4200/patient/paiement?action=cancel&orderId=" + orderId;

        // HTML avec les deux boutons
        String htmlContent =
                "<!DOCTYPE html>" +
                        "<html>" +
                        "<head>" +
                        "<meta charset='UTF-8'>" +
                        "<style>" +
                        "body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 20px; }" +
                        ".container { max-width: 600px; margin: 0 auto; background: white; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }" +
                        ".header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }" +
                        ".header h1 { margin: 0; font-size: 24px; }" +
                        ".content { padding: 30px; }" +
                        ".order-details { background: #f8f9fa; padding: 15px; border-radius: 8px; margin: 20px 0; }" +
                        ".buttons { text-align: center; margin: 30px 0; }" +
                        ".btn-confirm { background-color: #28a745; color: white; padding: 14px 30px; text-decoration: none; border-radius: 5px; display: inline-block; margin: 0 10px; font-weight: bold; }" +
                        ".btn-cancel { background-color: #dc3545; color: white; padding: 14px 30px; text-decoration: none; border-radius: 5px; display: inline-block; margin: 0 10px; font-weight: bold; }" +
                        ".btn-confirm:hover { background-color: #218838; }" +
                        ".btn-cancel:hover { background-color: #c82333; }" +
                        ".footer { text-align: center; padding: 20px; color: #666; font-size: 12px; border-top: 1px solid #eee; }" +
                        "</style>" +
                        "</head>" +
                        "<body>" +
                        "<div class='container'>" +
                        "<div class='header'>" +
                        "<h1>🛍️ Diacare</h1>" +
                        "<p>Confirmation de commande</p>" +
                        "</div>" +
                        "<div class='content'>" +
                        "<h2>Bonjour,</h2>" +
                        "<p>Merci d'avoir commandé sur Diacare !</p>" +
                        "<div class='order-details'>" +
                        "<h3>Détails de la commande #" + orderId + "</h3>" +
                        "<p><strong>Montant total:</strong> " + total + " DT</p>" +
                        "<p><strong>Date:</strong> " + new java.util.Date() + "</p>" +
                        "</div>" +
                        "<p>Veuillez confirmer votre commande en cliquant sur l'un des boutons ci-dessous :</p>" +
                        "<div class='buttons'>" +
                        "<a href='" + confirmUrl + "' class='btn-confirm'>✅ Oui, confirmer ma commande</a>" +
                        "<a href='" + cancelUrl + "' class='btn-cancel'>❌ Non, annuler ma commande</a>" +
                        "</div>" +
                        "<p><strong>⚠️ Important :</strong> Si vous ne confirmez pas votre commande dans les 24 heures, elle sera automatiquement annulée.</p>" +
                        "</div>" +
                        "<div class='footer'>" +
                        "<p>Cet email a été envoyé automatiquement, merci de ne pas y répondre.</p>" +
                        "<p>© 2024 Diacare - Tous droits réservés</p>" +
                        "</div>" +
                        "</div>" +
                        "</body>" +
                        "</html>";

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject("Confirmation de commande #" + orderId);
            helper.setText(htmlContent, true); // true = contenu HTML
            mailSender.send(message);
            log.info("Email de commande envoyé avec succès à: {}", email);
        } catch (MessagingException e) {
            log.error("Erreur lors de l'envoi de l'email à {}: {}", email, e.getMessage());
            throw new RuntimeException("Erreur lors de l'envoi de l'email", e);
        }
    }

    @Override
    public void sendOrderConfirmationEmail(String email, Long orderId) {
        log.info("Envoi d'email de confirmation à: {}", email);

        String confirmUrl = "http://localhost:4200/patient/paiement?action=confirm&orderId=" + orderId;
        String cancelUrl = "http://localhost:4200/patient/paiement?action=cancel&orderId=" + orderId;

        String htmlContent =
                "<!DOCTYPE html>" +
                        "<html>" +
                        "<head>" +
                        "<meta charset='UTF-8'>" +
                        "<style>" +
                        "body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 20px; }" +
                        ".container { max-width: 600px; margin: 0 auto; background: white; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }" +
                        ".header { background: linear-gradient(135deg, #28a745 0%, #218838 100%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }" +
                        ".header h1 { margin: 0; font-size: 24px; }" +
                        ".content { padding: 30px; }" +
                        ".success-icon { text-align: center; font-size: 60px; margin: 20px 0; }" +
                        ".order-details { background: #f8f9fa; padding: 15px; border-radius: 8px; margin: 20px 0; }" +
                        ".buttons { text-align: center; margin: 30px 0; }" +
                        ".btn-confirm { background-color: #28a745; color: white; padding: 14px 30px; text-decoration: none; border-radius: 5px; display: inline-block; margin: 0 10px; font-weight: bold; }" +
                        ".btn-cancel { background-color: #dc3545; color: white; padding: 14px 30px; text-decoration: none; border-radius: 5px; display: inline-block; margin: 0 10px; font-weight: bold; }" +
                        ".btn-invoice { background-color: #17a2b8; color: white; padding: 12px 25px; text-decoration: none; border-radius: 5px; display: inline-block; margin-top: 15px; }" +
                        ".footer { text-align: center; padding: 20px; color: #666; font-size: 12px; border-top: 1px solid #eee; }" +
                        "</style>" +
                        "</head>" +
                        "<body>" +
                        "<div class='container'>" +
                        "<div class='header'>" +
                        "<h1>✅ Paiement Confirmé !</h1>" +
                        "<p>Diacare</p>" +
                        "</div>" +
                        "<div class='content'>" +
                        "<div class='success-icon'>🎉</div>" +
                        "<h2>Félicitations !</h2>" +
                        "<p>Votre paiement a été confirmé avec succès pour la commande #" + orderId + ".</p>" +
                        "<div class='order-details'>" +
                        "<h3>Récapitulatif de votre commande</h3>" +
                        "<p><strong>Numéro de commande:</strong> #" + orderId + "</p>" +
                        "<p><strong>Statut:</strong> <span style='color: #28a745;'>Payé et confirmé</span></p>" +
                        "</div>" +
                        "<div class='buttons'>" +
                        "<a href='http://localhost:4200/patient/orders' class='btn-invoice'>📄 Voir mes commandes</a>" +
                        "</div>" +
                        "<p>Vous pouvez télécharger votre facture depuis votre espace client.</p>" +
                        "<p>Merci pour votre confiance !</p>" +
                        "</div>" +
                        "<div class='footer'>" +
                        "<p>© 2024 Diacare - Tous droits réservés</p>" +
                        "</div>" +
                        "</div>" +
                        "</body>" +
                        "</html>";

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject("✅ Paiement confirmé - Commande #" + orderId);
            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("Email de confirmation envoyé avec succès à: {}", email);
        } catch (MessagingException e) {
            log.error("Erreur lors de l'envoi de l'email de confirmation à {}: {}", email, e.getMessage());
            throw new RuntimeException("Erreur lors de l'envoi de l'email de confirmation", e);
        }
    }
}