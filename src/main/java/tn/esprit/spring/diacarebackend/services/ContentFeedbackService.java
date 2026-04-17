package tn.esprit.spring.diacarebackend.services;

import tn.esprit.spring.diacarebackend.dto.ContentEmotionStatsDTO;
import tn.esprit.spring.diacarebackend.dto.ContentFeedbackRequestDTO;
import tn.esprit.spring.diacarebackend.dto.EmotionalDashboardDTO;
import tn.esprit.spring.diacarebackend.entities.ContentFeedback;
import tn.esprit.spring.diacarebackend.entities.Emotion;
import tn.esprit.spring.diacarebackend.entities.EducationalContent;
import tn.esprit.spring.diacarebackend.repository.ContentFeedbackRepository;
import tn.esprit.spring.diacarebackend.repository.EducationalContentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Service
public class ContentFeedbackService {

    private final ContentFeedbackRepository feedbackRepository;
    private final EducationalContentRepository contentRepository;

    public ContentFeedbackService(ContentFeedbackRepository feedbackRepository,
                                  EducationalContentRepository contentRepository) {
        this.feedbackRepository = feedbackRepository;
        this.contentRepository = contentRepository;
    }

    @Transactional
    public ContentFeedback submitFeedback(Long contentId, ContentFeedbackRequestDTO request) {
        EducationalContent content = contentRepository.findById(contentId)
                .orElseThrow(() -> new RuntimeException("Contenu non trouvé"));

        ContentFeedback feedback = new ContentFeedback();
        feedback.setContentId(content.getId());
        feedback.setPatientId(request.getUserId() != null ? request.getUserId() : 1L);
        feedback.setEmotion(normalizeEmotion(request.getEmotion()));
        feedback.setComment(request.getCommentaire());
        return feedbackRepository.save(feedback);
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

    private ContentEmotionStatsDTO createEmptyStats(Long contentId) {
        ContentEmotionStatsDTO stats = new ContentEmotionStatsDTO();
        stats.setContentId(contentId);
        contentRepository.findById(contentId).ifPresent(content -> {
            stats.setContentTitle(content.getTitle());
            stats.setCategory(content.getCategory() != null ? content.getCategory().name() : "");
        });
        return stats;
    }

    private Emotion normalizeEmotion(String rawEmotion) {
        if (rawEmotion == null) {
            return Emotion.NEUTRAL;
        }

        String emotion = rawEmotion.trim().toLowerCase(Locale.ROOT);
        return switch (emotion) {
            case "😀", ":)", "happy", "satisfied", "positif", "positive", "joy", "smile" -> Emotion.HAPPY;
            case "😟", ":(", "sad", "anxious", "negative", "triste", "worried" -> Emotion.SAD;
            case "😐", "neutral", "meh", "moyen", "normal" -> Emotion.NEUTRAL;
            default -> Emotion.NEUTRAL;
        };
    }
}
