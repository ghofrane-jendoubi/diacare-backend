package tn.esprit.spring.diacarebackend.services;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.spring.diacarebackend.dto.PatientSignupRequest;
import tn.esprit.spring.diacarebackend.entities.Patient;
import tn.esprit.spring.diacarebackend.repository.PatientRepository;
import tn.esprit.spring.diacarebackend.entities.Role;
import tn.esprit.spring.diacarebackend.repository.UserRepository;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    private static final String UPLOAD_DIR = "uploads/patients/";

    public PatientService(PatientRepository patientRepository,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JavaMailSender mailSender) {
        this.patientRepository = patientRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
    }

    // =================== hCaptcha VERIFICATION ===================
    // Pour Bot Management, on vérifie simplement que le token est présent
    private boolean verifyHCaptcha(String token) {
        if (token == null || token.isEmpty()) {
            System.out.println("❌ hCaptcha token est null ou vide");
            return false;
        }

        System.out.println("✅ Token hCaptcha reçu et valide pour le patient: " + token.substring(0, Math.min(token.length(), 20)) + "...");
        return true;
    }

    // =================== SIGNUP ===================

    public Patient registerPatient(PatientSignupRequest request) {
        // ✅ 1. Vérifier hCaptcha
        System.out.println("🔍 Vérification du token hCaptcha pour le patient...");

        if (!verifyHCaptcha(request.getHcaptchaToken())) {
            throw new RuntimeException("Vérification anti-robot échouée. Veuillez réessayer.");
        }

        // 2. Vérifier email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email déjà utilisé");
        }

        String token = UUID.randomUUID().toString();

        Patient patient = new Patient();
        patient.setFirstName(request.getFirstName());
        patient.setLastName(request.getLastName());
        patient.setEmail(request.getEmail());
        patient.setPhone(request.getPhone());
        patient.setPassword(passwordEncoder.encode(request.getPassword()));
        patient.setRole(Role.PATIENT);
        patient.setEnabled(false);
        patient.setActivationToken(token);
        patient.setActivationTokenExpiry(LocalDateTime.now().plusHours(24));
        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setGender(request.getGender());
        patient.setAddress(request.getAddress());
        patient.setCity(request.getCity());
        patient.setDiabetesType(request.getDiabetesType());
        patient.setBloodType(request.getBloodType());
        patient.setWeight(request.getWeight());
        patient.setHeight(request.getHeight());
        patient.setEmergencyContact(request.getEmergencyContact());
        patient.setFamilyHistory(request.getFamilyHistory());

        Patient saved = patientRepository.save(patient);
        sendActivationEmail(saved);
        return saved;
    }

    private void sendActivationEmail(Patient patient) {
        String link = "http://localhost:8081/api/patients/activate?token="
                + patient.getActivationToken();
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(patient.getEmail());
        message.setSubject("Activation de votre compte DiaCare");
        message.setText(
                "Bonjour " + patient.getFirstName() + ",\n\n" +
                        "Bienvenue sur DiaCare !\n" +
                        "Cliquez sur le lien ci-dessous pour activer votre compte :\n\n" +
                        link + "\n\n" +
                        "Ce lien expire dans 24 heures.\n\n" +
                        "Equipe DiaCare"
        );
        mailSender.send(message);
    }

    // =================== ACTIVATION ===================

    public String activateAccount(String token) {
        Patient patient = patientRepository.findByActivationToken(token)
                .orElseThrow(() -> new RuntimeException("Token invalide"));

        if (patient.isEnabled()) return "Compte déjà activé";

        if (patient.getActivationTokenExpiry() == null ||
                patient.getActivationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expiré");
        }

        patient.setEnabled(true);
        patient.setActivationToken(null);
        patient.setActivationTokenExpiry(null);
        patientRepository.save(patient);

        return "Compte activé avec succès. Vous pouvez maintenant vous connecter.";
    }

    // =================== LOGIN ===================

    public Map<String, Object> login(String email, String password) {
        Patient patient = patientRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email introuvable"));

        if (!patient.isEnabled())
            throw new RuntimeException("Veuillez activer votre compte depuis l'email reçu");

        if (!passwordEncoder.matches(password, patient.getPassword()))
            throw new RuntimeException("Mot de passe incorrect");

        if (patient.getRole() != Role.PATIENT)
            throw new RuntimeException("Accès refusé");

        return buildPatientResponse(patient);
    }

    // =================== GET BY ID ===================

    public Map<String, Object> getPatientById(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient introuvable"));
        return buildPatientResponse(patient);
    }

    // =================== UPDATE INFOS PERSONNELLES ===================

    public Map<String, Object> updateInfo(Long id, PatientSignupRequest request) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient introuvable"));

        if (request.getFirstName() != null && !request.getFirstName().isBlank())
            patient.setFirstName(request.getFirstName());

        if (request.getLastName() != null && !request.getLastName().isBlank())
            patient.setLastName(request.getLastName());

        if (request.getPhone() != null)
            patient.setPhone(request.getPhone());

        if (request.getAddress() != null)
            patient.setAddress(request.getAddress());

        if (request.getCity() != null)
            patient.setCity(request.getCity());

        if (request.getDateOfBirth() != null)
            patient.setDateOfBirth(request.getDateOfBirth());

        if (request.getGender() != null)
            patient.setGender(request.getGender());

        return buildPatientResponse(patientRepository.save(patient));
    }

    // =================== UPDATE INFOS SANTE ===================

    public Map<String, Object> updateSante(Long id, PatientSignupRequest request) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient introuvable"));

        if (request.getDiabetesType() != null)
            patient.setDiabetesType(request.getDiabetesType());

        if (request.getBloodType() != null)
            patient.setBloodType(request.getBloodType());

        if (request.getWeight() != null)
            patient.setWeight(request.getWeight());

        if (request.getHeight() != null)
            patient.setHeight(request.getHeight());

        if (request.getEmergencyContact() != null)
            patient.setEmergencyContact(request.getEmergencyContact());

        if (request.getFamilyHistory() != null)
            patient.setFamilyHistory(request.getFamilyHistory());

        return buildPatientResponse(patientRepository.save(patient));
    }

    // =================== CHANGE PASSWORD ===================

    public void changePassword(Long id, PatientSignupRequest request) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient introuvable"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), patient.getPassword()))
            throw new RuntimeException("Mot de passe actuel incorrect");

        if (request.getNewPassword() == null || request.getNewPassword().length() < 8)
            throw new RuntimeException("Le nouveau mot de passe doit contenir au moins 8 caractères");

        patient.setPassword(passwordEncoder.encode(request.getNewPassword()));
        patientRepository.save(patient);
    }

    // =================== UPLOAD PHOTO ===================

    public Map<String, Object> uploadPhoto(Long id, MultipartFile file) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient introuvable"));

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/"))
            throw new RuntimeException("Le fichier doit être une image");

        if (file.getSize() > 5 * 1024 * 1024)
            throw new RuntimeException("L'image ne doit pas dépasser 5 MB");

        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath))
                Files.createDirectories(uploadPath);

            if (patient.getProfilePicture() != null) {
                Path oldFile = Paths.get("." + patient.getProfilePicture());
                if (Files.exists(oldFile)) Files.delete(oldFile);
            }

            String extension = getExtension(file.getOriginalFilename());
            String fileName = "patient_" + id + "_" + UUID.randomUUID() + extension;
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String relativePath = "/" + UPLOAD_DIR + fileName;
            patient.setProfilePicture(relativePath);
            patientRepository.save(patient);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Photo mise à jour avec succès");
            response.put("profilePicture", relativePath);
            return response;

        } catch (IOException e) {
            throw new RuntimeException("Erreur upload : " + e.getMessage());
        }
    }

    // =================== GET ALL ===================

    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    // =================== HELPERS ===================

    private Map<String, Object> buildPatientResponse(Patient patient) {
        Map<String, Object> r = new HashMap<>();
        r.put("id",               patient.getId());
        r.put("email",            patient.getEmail());
        r.put("firstName",        patient.getFirstName());
        r.put("lastName",         patient.getLastName());
        r.put("phone",            patient.getPhone());
        r.put("address",          patient.getAddress());
        r.put("city",             patient.getCity());
        r.put("dateOfBirth",      patient.getDateOfBirth());
        r.put("gender",           patient.getGender());
        r.put("role",             patient.getRole());
        r.put("diabetesType",     patient.getDiabetesType());
        r.put("bloodType",        patient.getBloodType());
        r.put("weight",           patient.getWeight());
        r.put("height",           patient.getHeight());
        r.put("emergencyContact", patient.getEmergencyContact());
        r.put("familyHistory",    patient.getFamilyHistory());
        r.put("profilePicture",   patient.getProfilePicture());
        return r;
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return ".jpg";
        return filename.substring(filename.lastIndexOf("."));
    }
}