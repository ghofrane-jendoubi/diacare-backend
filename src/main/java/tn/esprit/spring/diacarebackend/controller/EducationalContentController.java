package tn.esprit.spring.diacarebackend.controller;

import tn.esprit.spring.diacarebackend.DTOs.ContentDTO;
import tn.esprit.spring.diacarebackend.DTOs.ContentSummaryDTO;
import tn.esprit.spring.diacarebackend.Service.EducationalContentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/education")
@CrossOrigin(origins = "http://localhost:4200")
public class EducationalContentController {
    private static final Logger LOGGER = LoggerFactory.getLogger(EducationalContentController.class);

    private final EducationalContentService contentService;

    public EducationalContentController(EducationalContentService contentService) {
        this.contentService = contentService;
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
}