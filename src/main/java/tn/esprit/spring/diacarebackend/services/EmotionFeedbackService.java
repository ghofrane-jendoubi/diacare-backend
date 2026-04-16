package tn.esprit.spring.diacarebackend.services;

import tn.esprit.spring.diacarebackend.dto.ContentEmotionStatsDTO;
import tn.esprit.spring.diacarebackend.dto.EmotionalDashboardDTO;
import tn.esprit.spring.diacarebackend.dto.PatientFeedbackDto;
import tn.esprit.spring.diacarebackend.dto.FeedbackRequest;
import tn.esprit.spring.diacarebackend.dto.EmotionalEvolutionDTO;
import tn.esprit.spring.diacarebackend.entities.ContentFeedback;
import tn.esprit.spring.diacarebackend.entities.Emotion;
import tn.esprit.spring.diacarebackend.entities.AppUser;
import tn.esprit.spring.diacarebackend.entities.EducationalContent;
import tn.esprit.spring.diacarebackend.repository.ContentFeedbackRepository;
import tn.esprit.spring.diacarebackend.repository.EducationalContentRepository;
import tn.esprit.spring.diacarebackend.repository.AppUserRepository;
import tn.esprit.spring.diacarebackend.repository.PatientEmotionalStateRepository;
import tn.esprit.spring.diacarebackend.entities.PatientEmotionalState;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EmotionFeedbackService {

    private final ContentFeedbackRepository feedbackRepository;
    private final EducationalContentRepository contentRepository;
    private final AppUserRepository appUserRepository;
    private final PatientEmotionalStateRepository emotionalStateRepository;

    public EmotionFeedbackService(ContentFeedbackRepository feedbackRepository,
                                  EducationalContentRepository contentRepository,
                                  AppUserRepository appUserRepository,
                                  PatientEmotionalStateRepository emotionalStateRepository) {
        this.feedbackRepository = feedbackRepository;
        this.contentRepository = contentRepository;
        this.appUserRepository = appUserRepository;
        this.emotionalStateRepository = emotionalStateRepository;
    }

    public boolean hasFeedback(Long contentId, Long userId) {
        if (contentId == null || userId == null) {
            return false;
        }
        return feedbackRepository.existsByContentIdAndPatientId(contentId, userId);
    }

    @Transactional
    public ContentFeedback submitFeedback(Long contentId, Long userId, FeedbackRequest request) {
        if (contentId == null) {
            throw new IllegalArgumentException("Contenu non trouve");
        }

        if (userId == null) {
            throw new IllegalArgumentException("Utilisateur non authentifié");
        }

        if (request == null) {
            throw new IllegalArgumentException("Emotion invalide");
        }

        contentRepository.findById(contentId)
                .orElseThrow(() -> new IllegalArgumentException("Contenu non trouvé"));

        if (feedbackRepository.existsByContentIdAndPatientId(contentId, userId)) {
            return feedbackRepository.findByPatientIdAndContentId(userId, contentId)
                    .orElseThrow(() -> new IllegalStateException("Feedback déjà existant mais introuvable"));
        }

        ContentFeedback feedback = new ContentFeedback();
        feedback.setContentId(contentId);
        feedback.setPatientId(userId);
        feedback.setEmotion(request.getEmotion() != null ? request.getEmotion() : Emotion.NEUTRAL);
        feedback.setComment(request.getComment());

        try {
            ContentFeedback savedFeedback = feedbackRepository.save(feedback);
            // Mettre à jour l'état émotionnel du patient
            updateEmotionalState(userId);
            return savedFeedback;
        } catch (DataIntegrityViolationException e) {
            return feedbackRepository.findByPatientIdAndContentId(userId, contentId)
                    .orElseThrow(() -> new IllegalStateException("Impossible de récupérer le feedback existant", e));
        }
    }

    private void updateEmotionalState(Long patientId) {
        // Récupérer les 5 derniers feedbacks du patient
        List<ContentFeedback> lastFeedbacks = feedbackRepository.findTop5ByPatientIdOrderByCreatedAtDesc(patientId);
        if (lastFeedbacks.isEmpty()) return;

        double sum = 0.0;
        for (ContentFeedback fb : lastFeedbacks) {
            switch (fb.getEmotion()) {
                case HAPPY -> sum += 1.0;
                case SAD -> sum += -1.0;
                default -> sum += 0.0;
            }
        }
        double avg = sum / lastFeedbacks.size();

        PatientEmotionalState state = emotionalStateRepository.findByPatientId(patientId)
                .orElse(new PatientEmotionalState());
        state.setPatientId(patientId);
        state.setAverageScore(avg);
        state.setLastFeedbacksCount(lastFeedbacks.size());
        state.setLastUpdate(LocalDateTime.now());
        emotionalStateRepository.save(state);
    }

    public EmotionalDashboardDTO getEmotionalDashboard() {
        List<ContentFeedback> feedbacks = feedbackRepository.findAllByOrderByCreatedAtDesc();
        Map<Long, ContentEmotionStatsDTO> statsByContent = new HashMap<>();

        long happyCount = 0;
        long neutralCount = 0;
        long sadCount = 0;

        for (ContentFeedback feedback : feedbacks) {
            if (feedback == null || feedback.getContentId() == null) {
                continue;
            }

            ContentEmotionStatsDTO stats = statsByContent.computeIfAbsent(feedback.getContentId(), this::createEmptyStats);
            stats.setFeedbackCount(stats.getFeedbackCount() + 1);

            switch (feedback.getEmotion()) {
                case HAPPY -> {
                    stats.setHappyCount(stats.getHappyCount() + 1);
                    happyCount++;
                }
                case SAD -> {
                    stats.setSadCount(stats.getSadCount() + 1);
                    sadCount++;
                }
                default -> {
                    stats.setNeutralCount(stats.getNeutralCount() + 1);
                    neutralCount++;
                }
            }
        }

        List<ContentEmotionStatsDTO> contentStats = statsByContent.values().stream()
                .peek(stats -> {
                    if (stats.getFeedbackCount() > 0) {
                        stats.setAnxietyRate((stats.getSadCount() * 100.0) / stats.getFeedbackCount());
                    }
                })
                .sorted(Comparator.comparingDouble(ContentEmotionStatsDTO::getAnxietyRate).reversed()
                        .thenComparing(ContentEmotionStatsDTO::getFeedbackCount, Comparator.reverseOrder()))
                .toList();

        EmotionalDashboardDTO dashboard = new EmotionalDashboardDTO();
        dashboard.setTotalFeedbacks(feedbacks.size());
        dashboard.setHappyCount(happyCount);
        dashboard.setNeutralCount(neutralCount);
        dashboard.setSadCount(sadCount);
        dashboard.setContentStats(contentStats);
        return dashboard;
    }

    public List<PatientFeedbackDto> getDoctorFeedbacks(Long doctorId) {
        if (doctorId == null) {
            return List.of();
        }

        List<EducationalContent> doctorContents = contentRepository.findByAuthorIdOrderByCreatedAtDesc(doctorId);
        if (doctorContents.isEmpty()) {
            return List.of();
        }

        Map<Long, EducationalContent> contentById = doctorContents.stream()
                .collect(Collectors.toMap(EducationalContent::getId, content -> content));

        List<Long> contentIds = doctorContents.stream()
                .map(EducationalContent::getId)
                .collect(Collectors.toList());

        return feedbackRepository.findByContentIdIn(contentIds).stream()
                .sorted(Comparator.comparing(ContentFeedback::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(feedback -> {
                    EducationalContent content = contentById.get(feedback.getContentId());
                    AppUser patient = appUserRepository.findById(feedback.getPatientId()).orElse(null);

                    PatientFeedbackDto dto = new PatientFeedbackDto();
                    dto.setPatientId(feedback.getPatientId());
                    dto.setPatientName(resolveDisplayName(patient));
                    dto.setContentId(feedback.getContentId());
                    dto.setContentTitle(content != null ? content.getTitle() : "Contenu supprimé");
                    dto.setEmotion(feedback.getEmotion());
                    dto.setComment(feedback.getComment());
                    dto.setCreatedAt(feedback.getCreatedAt());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private ContentEmotionStatsDTO createEmptyStats(Long contentId) {
        ContentEmotionStatsDTO stats = new ContentEmotionStatsDTO();
        stats.setContentId(contentId);
        contentRepository.findById(contentId).ifPresent(content -> {
            stats.setContentTitle(content.getTitle());
            stats.setCategory(content.getCategory() != null ? content.getCategory().name() : "");
        });
        return stats;
    }

    private Emotion parseEmotion(String rawEmotion) {
        if (rawEmotion == null || rawEmotion.isBlank()) {
            throw new IllegalArgumentException("Émotion invalide");
        }

        String emotion = rawEmotion.trim().toLowerCase(Locale.ROOT);
        return switch (emotion) {
            case "😀", ":)", "happy", "satisfied", "positif", "positive", "joy", "smile" -> Emotion.HAPPY;
            case "😟", ":(", "sad", "anxious", "negative", "triste", "worried" -> Emotion.SAD;
            case "😐", "neutral", "meh", "moyen", "normal" -> Emotion.NEUTRAL;
            default -> throw new IllegalArgumentException("Émotion invalide");
        };
    }

    private String resolveDisplayName(AppUser user) {
        if (user == null) {
            return "Patient";
        }

        if (user.getName() != null && !user.getName().isBlank()) {
            return user.getName();
        }

        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            return user.getEmail();
        }

        return "Patient";
    }

    // ===== MÉTHODE POUR LE DASHBOARD MÉDECIN - ÉVOLUTION ÉMOTIONNELLE =====

    public List<EmotionalEvolutionDTO> getEmotionalEvolution(Long patientId) {
        if (patientId == null) {
            return new ArrayList<>();
        }

        // Récupérer tous les feedbacks du patient triés par date
        List<ContentFeedback> feedbacks = feedbackRepository.findByPatientIdOrderByCreatedAtAsc(patientId);
        List<EmotionalEvolutionDTO> evolution = new ArrayList<>();

        for (ContentFeedback fb : feedbacks) {
            double score = switch (fb.getEmotion()) {
                case HAPPY -> 1.0;
                case SAD -> -1.0;
                default -> 0.0;
            };

            evolution.add(new EmotionalEvolutionDTO(
                fb.getCreatedAt(),
                score,
                fb.getContentId(),
                fb.getEmotion() != null ? fb.getEmotion().name() : "NEUTRAL"
            ));
        }

        return evolution;
    }
}
