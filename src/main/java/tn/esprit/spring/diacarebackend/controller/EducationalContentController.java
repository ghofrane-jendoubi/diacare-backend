package tn.esprit.spring.diacarebackend.controller;

import tn.esprit.spring.diacarebackend.DTOs.ContentDTO;
import tn.esprit.spring.diacarebackend.DTOs.ContentSummaryDTO;
import tn.esprit.spring.diacarebackend.Service.EducationalContentService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/education")
@CrossOrigin(origins = "http://localhost:4200")
public class EducationalContentController {

    private final EducationalContentService contentService;

    // Constructeur manuel
    public EducationalContentController(EducationalContentService contentService) {
        this.contentService = contentService;
    }

    @GetMapping("/contents")
    public ResponseEntity<Page<ContentSummaryDTO>> getAllContents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            @RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(contentService.getAllContents(page, size, userId));
    }

    @GetMapping("/contents/category/{category}")
    public ResponseEntity<Page<ContentSummaryDTO>> getByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            @RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(contentService.getByCategory(category, page, size, userId));
    }

    @GetMapping("/contents/search")
    public ResponseEntity<Page<ContentSummaryDTO>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            @RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(contentService.search(keyword, page, size, userId));
    }

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

    @GetMapping("/my-bookmarks")
    public ResponseEntity<List<ContentSummaryDTO>> getMyBookmarks(
            @RequestParam(defaultValue = "1") Long userId) {
        return ResponseEntity.ok(contentService.getUserBookmarks(userId));
    }
}