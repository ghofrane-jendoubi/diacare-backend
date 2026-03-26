package tn.esprit.spring.diacarebackend.admin.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tn.esprit.spring.diacarebackend.admin.dto.AdminSignupRequest;
import tn.esprit.spring.diacarebackend.admin.entity.Admin;
import tn.esprit.spring.diacarebackend.admin.repository.AdminRepository;
import tn.esprit.spring.diacarebackend.user.entity.Role;
import tn.esprit.spring.diacarebackend.user.entity.User;
import tn.esprit.spring.diacarebackend.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AdminSignupService {

    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    public AdminSignupService(AdminRepository adminRepository,
                              UserRepository userRepository,
                              PasswordEncoder passwordEncoder,
                              JavaMailSender mailSender) {
        this.adminRepository = adminRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
    }

    public Admin registerAdmin(AdminSignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email déjà utilisé");
        }

        String token = UUID.randomUUID().toString();

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.ADMIN);
        user.setEnabled(false);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setActivationToken(token);
        user.setActivationTokenExpiry(LocalDateTime.now().plusHours(24));

        User savedUser = userRepository.save(user);

        Admin admin = new Admin();
        admin.setUser(savedUser);
        Admin savedAdmin = adminRepository.save(admin);

        sendActivationEmail(savedUser);

        return savedAdmin;
    }

    private void sendActivationEmail(User user) {
        String activationLink = "http://localhost:8081/api/admins/activate?token=" + user.getActivationToken();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Activation de votre compte admin");
        message.setText(
                "Bonjour " + user.getFirstName() + ",\n\n" +
                        "Merci pour votre inscription.\n" +
                        "Veuillez cliquer sur le lien ci-dessous pour activer votre compte :\n\n" +
                        activationLink + "\n\n" +
                        "Ce lien expire dans 24 heures.\n\n" +
                        "Equipe DiaCare"
        );

        mailSender.send(message);
    }

    public String activateAccount(String token) {
        User user = userRepository.findByActivationToken(token)
                .orElseThrow(() -> new RuntimeException("Token invalide"));

        if (user.isEnabled()) {
            return "Compte déjà activé";
        }

        if (user.getActivationTokenExpiry() == null || user.getActivationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expiré");
        }

        user.setEnabled(true);
        user.setActivationToken(null);
        user.setActivationTokenExpiry(null);
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);

        return "Compte activé avec succès";
    }

    public Map<String, Object> login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email introuvable"));

        if (!user.isEnabled()) {
            throw new RuntimeException("Veuillez activer votre compte depuis l'email reçu");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Mot de passe incorrect");
        }

        if (user.getRole() != Role.ADMIN) {
            throw new RuntimeException("Accès refusé : utilisateur non admin");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Connexion réussie");
        response.put("id", user.getId());
        response.put("email", user.getEmail());
        response.put("firstName", user.getFirstName());
        response.put("lastName", user.getLastName());
        response.put("role", user.getRole());

        return response;
    }
}