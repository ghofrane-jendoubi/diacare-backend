package tn.esprit.spring.diacarebackend.doctor.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.spring.diacarebackend.doctor.dto.DoctorSignupRequest;
import tn.esprit.spring.diacarebackend.doctor.entity.CertificateStatus;
import tn.esprit.spring.diacarebackend.doctor.entity.Doctor;
import tn.esprit.spring.diacarebackend.doctor.repository.DoctorRepository;
import tn.esprit.spring.diacarebackend.user.entity.Role;
import tn.esprit.spring.diacarebackend.user.repository.UserRepository;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class DoctorSignupService {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    private static final String UPLOAD_DIR = "uploads/certificates/";

    public DoctorSignupService(DoctorRepository doctorRepository,
                               UserRepository userRepository,
                               PasswordEncoder passwordEncoder,
                               JavaMailSender mailSender) {
        this.doctorRepository = doctorRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
    }

    // =================== SIGNUP ===================

    public Doctor registerDoctor(DoctorSignupRequest request, MultipartFile certificateImage) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email déjà utilisé");
        }

        if (certificateImage == null || certificateImage.isEmpty()) {
            throw new RuntimeException("Le certificat est obligatoire");
        }

        String imagePath = saveCertificateImage(certificateImage);
        String token = UUID.randomUUID().toString();

        // Doctor EST un User — on remplit directement
        Doctor doctor = new Doctor();

        // Champs User (hérités)
        doctor.setFirstName(request.getFirstName());
        doctor.setLastName(request.getLastName());
        doctor.setEmail(request.getEmail());
        doctor.setPhone(request.getPhone());
        doctor.setPassword(passwordEncoder.encode(request.getPassword()));
        doctor.setRole(Role.DOCTOR);
        doctor.setEnabled(false);
        doctor.setActivationToken(token);
        doctor.setActivationTokenExpiry(LocalDateTime.now().plusHours(24));

        // Champs Doctor spécifiques
        doctor.setSpeciality(request.getSpeciality());
        doctor.setLicenseNumber(request.getLicenseNumber());
        doctor.setYearsOfExperience(request.getYearsOfExperience());
        doctor.setConsultationFee(request.getConsultationFee());
        doctor.setHospital(request.getHospital());
        doctor.setCertificateImage(imagePath);
        doctor.setCertificateStatus(CertificateStatus.PENDING);

        Doctor savedDoctor = doctorRepository.save(doctor);

        sendActivationEmail(savedDoctor);

        return savedDoctor;
    }

    private String saveCertificateImage(MultipartFile file) {
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            return UPLOAD_DIR + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Erreur sauvegarde certificat : " + e.getMessage());
        }
    }

    // =================== ACTIVATION ===================

    private void sendActivationEmail(Doctor doctor) {
        String link = "http://localhost:8081/api/doctors/activate?token=" + doctor.getActivationToken();
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(doctor.getEmail());
        message.setSubject("Activation de votre compte médecin - DiaCare");
        message.setText(
                "Bonjour " + doctor.getFirstName() + ",\n\n" +
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
        // findByActivationToken cherche dans la table users (héritage JOINED)
        Doctor doctor = doctorRepository.findByActivationToken(token)
                .orElseThrow(() -> new RuntimeException("Token invalide"));

        if (doctor.isEnabled()) return "Compte déjà activé";

        if (doctor.getActivationTokenExpiry() == null ||
                doctor.getActivationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expiré");
        }

        doctor.setEnabled(true);
        doctor.setActivationToken(null);
        doctor.setActivationTokenExpiry(null);
        doctorRepository.save(doctor);

        return "Compte activé. Votre certificat est en cours d'examen.";
    }

    // =================== LOGIN ===================

    public Map<String, Object> login(String email, String password) {
        Doctor doctor = doctorRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email introuvable"));

        if (!doctor.isEnabled())
            throw new RuntimeException("Veuillez activer votre compte depuis l'email reçu");

        if (!passwordEncoder.matches(password, doctor.getPassword()))
            throw new RuntimeException("Mot de passe incorrect");

        if (doctor.getRole() != Role.DOCTOR)
            throw new RuntimeException("Accès refusé");

        if (doctor.getCertificateStatus() == CertificateStatus.PENDING)
            throw new RuntimeException("Votre certificat est en cours d'examen. Veuillez patienter.");

        if (doctor.getCertificateStatus() == CertificateStatus.REJECTED)
            throw new RuntimeException("Votre certificat a été rejeté. Contactez l'administration.");

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Connexion réussie");
        response.put("id", doctor.getId());
        response.put("email", doctor.getEmail());
        response.put("firstName", doctor.getFirstName());
        response.put("lastName", doctor.getLastName());
        response.put("role", doctor.getRole());
        response.put("speciality", doctor.getSpeciality());
        response.put("hospital", doctor.getHospital());
        response.put("certificateStatus", doctor.getCertificateStatus());
        return response;
    }

    // =================== ADMIN ===================

    public void approveCertificate(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Médecin introuvable"));
        doctor.setCertificateStatus(CertificateStatus.APPROVED);
        doctorRepository.save(doctor);
        sendCertificateNotification(doctor, true);
    }

    public void rejectCertificate(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Médecin introuvable"));
        doctor.setCertificateStatus(CertificateStatus.REJECTED);
        doctorRepository.save(doctor);
        sendCertificateNotification(doctor, false);
    }

    public List<Doctor> getDoctorsByStatus(CertificateStatus status) {
        return doctorRepository.findByCertificateStatus(status);
    }

    private void sendCertificateNotification(Doctor doctor, boolean approved) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(doctor.getEmail());
        if (approved) {
            message.setSubject("Certificat approuvé - DiaCare");
            message.setText("Bonjour " + doctor.getFirstName() + ",\n\nVotre certificat a été approuvé. Vous pouvez maintenant vous connecter.\n\nEquipe DiaCare");
        } else {
            message.setSubject("Certificat rejeté - DiaCare");
            message.setText("Bonjour " + doctor.getFirstName() + ",\n\nVotre certificat a été rejeté. Contactez l'administration.\n\nEquipe DiaCare");
        }
        mailSender.send(message);
    }




    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    public Doctor getDoctorById(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Médecin introuvable"));
    }
}