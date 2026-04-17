package tn.esprit.spring.diacarebackend.services;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import tn.esprit.spring.diacarebackend.entities.Reclamation;

@Service
public class MailService {

    private final JavaMailSender mailSender;

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendReclamationCreatedMail(String toEmail, Reclamation reclamation) {
        if (toEmail == null || toEmail.isBlank()) return;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Confirmation de votre réclamation - DiaCare");
        message.setText(
                "Bonjour,\n\n" +
                        "Votre réclamation a bien été enregistrée sur DiaCare.\n\n" +
                        "Détails :\n" +
                        "- Titre : " + reclamation.getTitle() + "\n" +
                        "- Catégorie : " + reclamation.getCategory() + "\n" +
                        "- Priorité : " + reclamation.getPriority() + "\n" +
                        "- Statut : " + reclamation.getStatus() + "\n\n" +
                        "Nous vous informerons dès qu'une réponse sera ajoutée.\n\n" +
                        "Cordialement,\n" +
                        "L'équipe DiaCare"
        );

        mailSender.send(message);
    }

    public void sendReclamationResponseMail(String toEmail, Reclamation reclamation) {
        if (toEmail == null || toEmail.isBlank()) return;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Réponse à votre réclamation - DiaCare");
        message.setText(
                "Bonjour,\n\n" +
                        "Une réponse a été ajoutée à votre réclamation.\n\n" +
                        "Détails :\n" +
                        "- Titre : " + reclamation.getTitle() + "\n" +
                        "- Statut : " + reclamation.getStatus() + "\n\n" +
                        "Réponse :\n" +
                        (reclamation.getAdminResponse() != null ? reclamation.getAdminResponse() : "Aucune réponse") + "\n\n" +
                        "Cordialement,\n" +
                        "L'équipe DiaCare"
        );

        mailSender.send(message);
    }
}