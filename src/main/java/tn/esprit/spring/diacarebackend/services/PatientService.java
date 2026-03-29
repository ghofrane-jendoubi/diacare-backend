package tn.esprit.spring.diacarebackend.services;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tn.esprit.spring.diacarebackend.dto.PatientSignupRequest;
import tn.esprit.spring.diacarebackend.entities.Patient;
import tn.esprit.spring.diacarebackend.repository.PatientRepository;
import tn.esprit.spring.diacarebackend.entities.Role;
import tn.esprit.spring.diacarebackend.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    public PatientService(PatientRepository patientRepository,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JavaMailSender mailSender) {
        this.patientRepository = patientRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
    }
    public Optional<Patient> getPatientById(Long id) {
        return patientRepository.findById(id);
    }

    // =================== SIGNUP ===================

    public Patient registerPatient(PatientSignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email déjà utilisé");
        }

        String token = UUID.randomUUID().toString();

        Patient patient = new Patient();

        // Champs obligatoires
        patient.setFirstName(request.getFirstName());
        patient.setLastName(request.getLastName());
        patient.setEmail(request.getEmail());
        patient.setPhone(request.getPhone());
        patient.setPassword(passwordEncoder.encode(request.getPassword()));
        patient.setRole(Role.PATIENT);
        patient.setEnabled(false);
        patient.setActivationToken(token);
        patient.setActivationTokenExpiry(LocalDateTime.now().plusHours(24));

        // Champs optionnels — null si non renseigné, pas de risque de crash
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

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Connexion réussie");
        response.put("id", patient.getId());
        response.put("email", patient.getEmail());
        response.put("firstName", patient.getFirstName());
        response.put("lastName", patient.getLastName());
        response.put("role", patient.getRole());
        response.put("diabetesType", patient.getDiabetesType());
        response.put("bloodType", patient.getBloodType());
        return response;
    }



    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }
}