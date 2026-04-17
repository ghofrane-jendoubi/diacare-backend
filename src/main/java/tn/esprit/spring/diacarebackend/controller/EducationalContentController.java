package tn.esprit.spring.diacarebackend.controller;

import tn.esprit.spring.diacarebackend.dto.ContentDTO;
import tn.esprit.spring.diacarebackend.dto.FeedbackRequest;
import tn.esprit.spring.diacarebackend.dto.ContentSummaryDTO;
import tn.esprit.spring.diacarebackend.dto.EmotionalEvolutionDTO;
import tn.esprit.spring.diacarebackend.services.EmotionFeedbackService;
import tn.esprit.spring.diacarebackend.services.EducationalContentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/education", "/api/educational"})
@CrossOrigin(origins = "http://localhost:4200")
public class EducationalContentController {
    private static final Logger LOGGER = LoggerFactory.getLogger(EducationalContentController.class);

    private final EducationalContentService contentService;
    private final EmotionFeedbackService feedbackService;

    public EducationalContentController(EducationalContentService contentService,
                                       EmotionFeedbackService feedbackService) {
        this.contentService = contentService;
        this.feedbackService = feedbackService;
    }

    // ===== ROUTES SPÉCIFIQUES EN PREMIER =====

    @GetMapping("/contents/featured")
    public ResponseEntity<List<ContentSummaryDTO>> getFeatured(
            @RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(contentService.getFeaturedContents(userId));
    }

    @GetMapping("/contents/most-viewed")
    public ResponseEntity<List<ContentSummaryDTO>> getMostViewed(
            @RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(contentService.getMostViewed(userId));
    }

    @GetMapping("/recommendations")
    public ResponseEntity<List<ContentSummaryDTO>> getRecommendations(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String diabetesType) {
        return ResponseEntity.ok(contentService.getRecommendations(userId, diabetesType));
    }

    @GetMapping("/recommendations/adaptive")
    public ResponseEntity<List<ContentSummaryDTO>> getAdaptiveRecommendations(
            @RequestParam(required = false) Long patientId) {
        if (patientId == null) {
            return ResponseEntity.badRequest().body(List.of());
        }
        return ResponseEntity.ok(contentService.getAdaptiveRecommendations(patientId));
    }

    @GetMapping("/contents/search")
    public ResponseEntity<Page<ContentSummaryDTO>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            @RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(contentService.search(keyword, page, size, userId));
    }

    @GetMapping("/contents/category/{category}")
    public ResponseEntity<Page<ContentSummaryDTO>> getByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            @RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(contentService.getByCategory(category, page, size, userId));
    }

    @GetMapping("/my-bookmarks")
    public ResponseEntity<List<ContentSummaryDTO>> getMyBookmarks(
            @RequestParam(defaultValue = "1") Long userId) {
        return ResponseEntity.ok(contentService.getUserBookmarks(userId));
    }

    // ===== ROUTES GÉNÉRALES =====

    @GetMapping("/contents")
    public ResponseEntity<Page<ContentSummaryDTO>> getAllContents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            @RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(contentService.getAllContents(page, size, userId));
    }

    // ===== ROUTES AVEC {id} EN DERNIER =====

    @GetMapping("/contents/{id}")
    public ResponseEntity<ContentDTO> getDetail(
            @PathVariable Long id,
            @RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(contentService.getContentDetail(id, userId));
    }

    @PostMapping("/contents/{id}/like")
    public ResponseEntity<Map<String, Object>> toggleLike(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Long userId) {
        boolean liked = contentService.toggleLike(id, userId);
        return ResponseEntity.ok(Map.of("liked", liked));
    }

    @PostMapping("/contents/{id}/bookmark")
    public ResponseEntity<Map<String, Object>> toggleBookmark(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Long userId) {
        boolean bookmarked = contentService.toggleBookmark(id, userId);
        return ResponseEntity.ok(Map.of("bookmarked", bookmarked));
    }

    @PostMapping("/contents/{id}/feedback")
    public ResponseEntity<Map<String, Object>> submitFeedback(
            @PathVariable Long id,
            @RequestParam(required = false) Long userId,
            @RequestBody FeedbackRequest body) {
        try {
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Utilisateur non authentifié"));
            }
            feedbackService.submitFeedback(id, userId, body);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            LOGGER.error("Failed to save feedback for content id {}", id, e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/contents/{id}/feedback/exists")
    public ResponseEntity<Map<String, Object>> checkFeedbackExists(
            @PathVariable Long id,
            @RequestParam(required = false) Long userId) {
        if (userId == null) {
            return ResponseEntity.ok(Map.of("exists", false));
        }
        return ResponseEntity.ok(Map.of("exists", feedbackService.hasFeedback(id, userId)));
    }

    @GetMapping("/contents/{id}/feedback-exists")
    public ResponseEntity<Map<String, Object>> checkFeedbackExistsAlias(
            @PathVariable Long id,
            @RequestParam(required = false) Long userId) {
        if (userId == null) {
            return ResponseEntity.ok(Map.of("exists", false));
        }
        return ResponseEntity.ok(Map.of("exists", feedbackService.hasFeedback(id, userId)));
    }

    @PostMapping("/contents/{id}/comments")
    @Transactional
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<Map<String, Object>> addComment(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {

        try {
            String commentText = (String) body.get("commentText");
            String userName = (String) body.get("userName");
            Long parentCommentId = body.get("parentCommentId") != null ?
                    Long.valueOf(body.get("parentCommentId").toString()) : null;

            if (commentText == null || commentText.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Le commentaire ne peut pas être vide"));
            }

            // Utiliser un userId par défaut pour les patients non authentifiés
            Long userId = 1L; // ID patient par défaut
            Object comment = contentService.addComment(id, commentText, userId, userName, parentCommentId);
            return ResponseEntity.ok(Map.of("success", true, "comment", comment));
        } catch (Exception e) {
            LOGGER.error("Failed to add comment for content id {}", id, e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // ===== ENDPOINTS MÉDECIN - ÉVOLUTION ÉMOTIONNELLE =====

    @GetMapping("/doctor/patient/emotional-evolution")
    public ResponseEntity<List<EmotionalEvolutionDTO>> getEmotionalEvolution(
            @RequestParam(required = false) Long patientId) {
        if (patientId == null) {
            return ResponseEntity.badRequest().body(List.of());
        }
        return ResponseEntity.ok(feedbackService.getEmotionalEvolution(patientId));
    }
}
