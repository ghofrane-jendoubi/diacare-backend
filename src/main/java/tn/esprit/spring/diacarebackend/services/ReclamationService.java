package tn.esprit.spring.diacarebackend.services;

import org.springframework.stereotype.Service;
import tn.esprit.spring.diacarebackend.entities.*;
import tn.esprit.spring.diacarebackend.repository.ReclamationRepository;
import tn.esprit.spring.diacarebackend.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReclamationService {

    private final ReclamationRepository reclamationRepository;
    private final UserRepository userRepository;
    private final MailService mailService;

    public ReclamationService(ReclamationRepository reclamationRepository,
                              UserRepository userRepository,
                              MailService mailService) {
        this.reclamationRepository = reclamationRepository;
        this.userRepository = userRepository;
        this.mailService = mailService;
    }

    public Reclamation create(Reclamation reclamation) {

        // Si doctor ou nutritionniste créent une réclamation,
        // elle va automatiquement vers l'admin
        if (reclamation.getCreatedByRole() == UserRole.DOCTOR
                || reclamation.getCreatedByRole() == UserRole.NUTRITIONIST) {
            reclamation.setTargetRole(UserRole.ADMIN);
        } else {
            // Cas patient
            if (reclamation.getCategory() == ReclamationCategory.PLAN_ALIMENTAIRE) {
                reclamation.setTargetRole(UserRole.NUTRITIONIST);
            } else if (reclamation.getCategory() == ReclamationCategory.CONSULTATION
                    || reclamation.getCategory() == ReclamationCategory.SUIVI_MEDICAL
                    || reclamation.getCategory() == ReclamationCategory.RENDEZ_VOUS) {
                if (reclamation.getTargetRole() == null) {
                    reclamation.setTargetRole(UserRole.DOCTOR);
                }
            } else if (reclamation.getCategory() == ReclamationCategory.TECHNIQUE
                    || reclamation.getCategory() == ReclamationCategory.FACTURATION) {
                reclamation.setTargetRole(UserRole.ADMIN);
            }
        }

        if (reclamation.getStatus() == null) {
            reclamation.setStatus(ReclamationStatus.OPEN);
        }

        if (reclamation.getPriority() == null) {
            reclamation.setPriority(ReclamationPriority.MEDIUM);
        }

        Reclamation saved = reclamationRepository.save(reclamation);

        // Mail seulement si patient
        if (saved.getCreatedByRole() == UserRole.PATIENT) {
            String patientEmail = getUserEmail(saved.getCreatedById());
            mailService.sendReclamationCreatedMail(patientEmail, saved);
        }

        return saved;
    }

    public List<Reclamation> getAll() {
        return reclamationRepository.findAll();
    }

    public Reclamation getById(Long id) {
        return reclamationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Réclamation introuvable avec l'id " + id));
    }

    public List<Reclamation> getMine(Long userId, UserRole role) {
        return reclamationRepository.findByCreatedByIdAndCreatedByRole(userId, role);
    }

    public List<Reclamation> getByTargetRole(UserRole role) {
        return reclamationRepository.findByTargetRole(role);
    }

    public Reclamation updateStatus(Long id, ReclamationStatus status, Long handledById, UserRole handledByRole) {
        Reclamation reclamation = getById(id);

        reclamation.setStatus(status);
        reclamation.setHandledById(handledById);
        reclamation.setHandledByRole(handledByRole);

        if (status == ReclamationStatus.RESOLVED) {
            reclamation.setResolvedAt(LocalDateTime.now());
        }

        return reclamationRepository.save(reclamation);
    }

    public Reclamation respond(Long id, String response, String internalNote, Long handledById, UserRole handledByRole) {
        Reclamation reclamation = getById(id);

        reclamation.setAdminResponse(response);
        reclamation.setInternalNote(internalNote);
        reclamation.setHandledById(handledById);
        reclamation.setHandledByRole(handledByRole);

        if (reclamation.getStatus() == ReclamationStatus.OPEN) {
            reclamation.setStatus(ReclamationStatus.ANSWERED);
        }

        Reclamation saved = reclamationRepository.save(reclamation);

        // Mail seulement si patient
        if (saved.getCreatedByRole() == UserRole.PATIENT) {
            String patientEmail = getUserEmail(saved.getCreatedById());
            mailService.sendReclamationResponseMail(patientEmail, saved);
        }

        return saved;
    }

    public void delete(Long id) {
        reclamationRepository.deleteById(id);
    }

    public Map<String, Long> getStats() {
        List<Reclamation> all = reclamationRepository.findAll();
        Map<String, Long> stats = new HashMap<>();

        stats.put("total", (long) all.size());
        stats.put("open", all.stream().filter(r -> r.getStatus() == ReclamationStatus.OPEN).count());
        stats.put("inProgress", all.stream().filter(r -> r.getStatus() == ReclamationStatus.IN_PROGRESS).count());
        stats.put("answered", all.stream().filter(r -> r.getStatus() == ReclamationStatus.ANSWERED).count());
        stats.put("resolved", all.stream().filter(r -> r.getStatus() == ReclamationStatus.RESOLVED).count());
        stats.put("rejected", all.stream().filter(r -> r.getStatus() == ReclamationStatus.REJECTED).count());

        stats.put("patient", all.stream().filter(r -> r.getCreatedByRole() == UserRole.PATIENT).count());
        stats.put("doctor", all.stream().filter(r -> r.getCreatedByRole() == UserRole.DOCTOR).count());
        stats.put("nutritionnist", all.stream().filter(r -> r.getCreatedByRole() == UserRole.NUTRITIONIST).count());
        stats.put("admin", all.stream().filter(r -> r.getCreatedByRole() == UserRole.ADMIN).count());

        return stats;
    }

    private String getUserEmail(Long userId) {
        if (userId == null) return null;

        return userRepository.findById(userId)
                .map(User::getEmail)
                .orElse(null);
    }
}