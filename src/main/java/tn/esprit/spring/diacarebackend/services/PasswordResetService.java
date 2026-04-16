package tn.esprit.spring.diacarebackend.services;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tn.esprit.spring.diacarebackend.entities.User;
import tn.esprit.spring.diacarebackend.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    public PasswordResetService(UserRepository userRepository,
                                PasswordEncoder passwordEncoder,
                                JavaMailSender mailSender) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
    }

    // ===== ÉTAPE 1 : envoyer le code par email =====
    public void sendResetCode(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Aucun compte trouvé avec cet email"));

        if (!user.isEnabled())
            throw new RuntimeException("Ce compte n'est pas encore activé");

        // Générer un code à 6 chiffres
        String code = String.format("%06d", new Random().nextInt(999999));

        user.setResetPasswordToken(code);
        user.setResetPasswordTokenExpiry(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        sendCodeByEmail(user, code);
    }

    private void sendCodeByEmail(User user, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Code de réinitialisation - DiaCare");
        message.setText(
                "Bonjour " + user.getFirstName() + ",\n\n" +
                        "Vous avez demandé à réinitialiser votre mot de passe.\n\n" +
                        "Votre code de vérification est :\n\n" +
                        "    " + code + "\n\n" +
                        "Ce code expire dans 15 minutes.\n" +
                        "Si vous n'êtes pas à l'origine de cette demande, ignorez cet email.\n\n" +
                        "Equipe DiaCare"
        );
        mailSender.send(message);
    }

    // ===== ÉTAPE 2 : vérifier le code =====
    public void verifyCode(String email, String code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email introuvable"));

        if (user.getResetPasswordToken() == null ||
                !user.getResetPasswordToken().equals(code))
            throw new RuntimeException("Code incorrect");

        if (user.getResetPasswordTokenExpiry() == null ||
                user.getResetPasswordTokenExpiry().isBefore(LocalDateTime.now()))
            throw new RuntimeException("Code expiré. Veuillez en demander un nouveau.");
    }

    // ===== ÉTAPE 3 : réinitialiser le mot de passe =====
    public void resetPassword(String email, String code, String newPassword) {
        // Vérifier le code d'abord
        verifyCode(email, code);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email introuvable"));

        if (newPassword == null || newPassword.length() < 8)
            throw new RuntimeException("Le mot de passe doit contenir au moins 8 caractères");

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetPasswordToken(null);
        user.setResetPasswordTokenExpiry(null);
        userRepository.save(user);
    }
}