package tn.esprit.spring.diacarebackend.services;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.spring.diacarebackend.entities.CertificateStatus;
import tn.esprit.spring.diacarebackend.dto.NutritionistSignupRequest;

import tn.esprit.spring.diacarebackend.entities.Nutritionist;
import tn.esprit.spring.diacarebackend.repository.NutritionistRepository;
import tn.esprit.spring.diacarebackend.entities.Role;
import tn.esprit.spring.diacarebackend.repository.UserRepository;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class NutritionistSignupService {

    private final NutritionistRepository nutritionistRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    private static final String UPLOAD_DIR = "uploads/certificates/nutritionists/";

    public NutritionistSignupService(NutritionistRepository nutritionistRepository,
                                     UserRepository userRepository,
                                     PasswordEncoder passwordEncoder,
                                     JavaMailSender mailSender) {
        this.nutritionistRepository = nutritionistRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
    }

    // =================== SIGNUP ===================

    public Nutritionist registerNutritionist(NutritionistSignupRequest request,
                                             MultipartFile certificateImage) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email déjà utilisé");
        }

        if (certificateImage == null || certificateImage.isEmpty()) {
            throw new RuntimeException("Le certificat est obligatoire");
        }

        String imagePath = saveCertificateImage(certificateImage);
        String token = UUID.randomUUID().toString();

        // Nutritionist EST un User — on remplit directement
        Nutritionist nutritionist = new Nutritionist();

        // Champs User hérités
        nutritionist.setFirstName(request.getFirstName());
        nutritionist.setLastName(request.getLastName());
        nutritionist.setEmail(request.getEmail());
        nutritionist.setPhone(request.getPhone());
        nutritionist.setPassword(passwordEncoder.encode(request.getPassword()));
        nutritionist.setRole(Role.NUTRITIONIST);
        nutritionist.setEnabled(false);
        nutritionist.setActivationToken(token);
        nutritionist.setActivationTokenExpiry(LocalDateTime.now().plusHours(24));

        // Champs Nutritionist spécifiques
        nutritionist.setLicenseNumber(request.getLicenseNumber());
        nutritionist.setYearsOfExperience(request.getYearsOfExperience());
        nutritionist.setWorkplace(request.getWorkplace());
        nutritionist.setWorkplaceAddress(request.getWorkplaceAddress());
        nutritionist.setCertificateImage(imagePath);
        nutritionist.setCertificateStatus(CertificateStatus.PENDING);

        Nutritionist saved = nutritionistRepository.save(nutritionist);
        sendActivationEmail(saved);
        return saved;
    }

    private String saveCertificateImage(MultipartFile file) {
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Files.copy(file.getInputStream(), uploadPath.resolve(fileName),
                    StandardCopyOption.REPLACE_EXISTING);
            return UPLOAD_DIR + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Erreur sauvegarde certificat : " + e.getMessage());
        }
    }

    // =================== ACTIVATION ===================

    private void sendActivationEmail(Nutritionist nutritionist) {
        String link = "http://localhost:8081/api/nutritionists/activate?token="
                + nutritionist.getActivationToken();
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(nutritionist.getEmail());
        message.setSubject("Activation de votre compte nutritionniste - DiaCare");
        message.setText(
                "Bonjour " + nutritionist.getFirstName() + ",\n\n" +
                        "Merci pour votre inscription sur DiaCare.\n" +
                        "Cliquez sur le lien ci-dessous pour activer votre compte :\n\n" +
                        link + "\n\n" +
                        "Après activation, votre certificat sera examiné par un administrateur.\n" +
                        "Ce lien expire dans 24 heures.\n\n" +
                        "Equipe DiaCare"
        );
        mailSender.send(message);
    }

    public String activateAccount(String token) {
        Nutritionist nutritionist = nutritionistRepository.findByActivationToken(token)
                .orElseThrow(() -> new RuntimeException("Token invalide"));

        if (nutritionist.isEnabled()) return "Compte déjà activé";

        if (nutritionist.getActivationTokenExpiry() == null ||
                nutritionist.getActivationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expiré");
        }

        nutritionist.setEnabled(true);
        nutritionist.setActivationToken(null);
        nutritionist.setActivationTokenExpiry(null);
        nutritionistRepository.save(nutritionist);

        return "Compte activé. Votre certificat est en cours d'examen.";
    }

    // =================== LOGIN ===================

    public Map<String, Object> login(String email, String password) {
        Nutritionist nutritionist = nutritionistRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email introuvable"));

        if (!nutritionist.isEnabled())
            throw new RuntimeException("Veuillez activer votre compte depuis l'email reçu");

        if (!passwordEncoder.matches(password, nutritionist.getPassword()))
            throw new RuntimeException("Mot de passe incorrect");

        if (nutritionist.getRole() != Role.NUTRITIONIST)
            throw new RuntimeException("Accès refusé");

        if (nutritionist.getCertificateStatus() == CertificateStatus.PENDING)
            throw new RuntimeException("Votre certificat est en cours d'examen. Veuillez patienter.");

        if (nutritionist.getCertificateStatus() == CertificateStatus.REJECTED)
            throw new RuntimeException("Votre certificat a été rejeté. Contactez l'administration.");

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Connexion réussie");
        response.put("id", nutritionist.getId());
        response.put("email", nutritionist.getEmail());
        response.put("firstName", nutritionist.getFirstName());
        response.put("lastName", nutritionist.getLastName());
        response.put("role", nutritionist.getRole());
        response.put("workplace", nutritionist.getWorkplace());
        response.put("certificateStatus", nutritionist.getCertificateStatus());
        return response;
    }

    // =================== ADMIN ===================

    public void approveCertificate(Long id) {
        Nutritionist nutritionist = nutritionistRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Nutritionniste introuvable"));
        nutritionist.setCertificateStatus(CertificateStatus.APPROVED);
        nutritionistRepository.save(nutritionist);
        sendCertificateNotification(nutritionist, true);
    }

    public void rejectCertificate(Long id) {
        Nutritionist nutritionist = nutritionistRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Nutritionniste introuvable"));
        nutritionist.setCertificateStatus(CertificateStatus.REJECTED);
        nutritionistRepository.save(nutritionist);
        sendCertificateNotification(nutritionist, false);
    }

    public List<Nutritionist> getByStatus(CertificateStatus status) {
        return nutritionistRepository.findByCertificateStatus(status);
    }

    private void sendCertificateNotification(Nutritionist nutritionist, boolean approved) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(nutritionist.getEmail());
        if (approved) {
            message.setSubject("Certificat approuvé - DiaCare");
            message.setText("Bonjour " + nutritionist.getFirstName() +
                    ",\n\nVotre certificat a été approuvé. Vous pouvez maintenant vous connecter.\n\nEquipe DiaCare");
        } else {
            message.setSubject("Certificat rejeté - DiaCare");
            message.setText("Bonjour " + nutritionist.getFirstName() +
                    ",\n\nVotre certificat a été rejeté. Contactez l'administration.\n\nEquipe DiaCare");
        }
        mailSender.send(message);
    }




    public List<Nutritionist> getAllNutritionists() {
        return nutritionistRepository.findAll();
    }

    public Nutritionist getNutritionistById(Long id) {
        return nutritionistRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Nutritionniste introuvable"));
    }
}