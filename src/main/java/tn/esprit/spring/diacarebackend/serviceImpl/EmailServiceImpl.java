package tn.esprit.spring.diacarebackend.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import tn.esprit.spring.diacarebackend.services.EmailService;


    @Service
    @RequiredArgsConstructor
    public class EmailServiceImpl implements EmailService {



            private final JavaMailSender mailSender;

            @Override
            public void sendOrderEmail(String email, Long orderId, double total) {

                SimpleMailMessage message = new SimpleMailMessage();

                message.setTo(email);
                message.setSubject("Confirmation de commande ✔");

                message.setText(
                        "Votre commande a été effectuée avec succès 🎉\n\n"
                                + "Numéro : " + orderId + "\n"
                                + "Total : " + total + " DT\n\n"
                                + "Merci pour votre confiance 💚"
                );

                mailSender.send(message);
            }
        }

