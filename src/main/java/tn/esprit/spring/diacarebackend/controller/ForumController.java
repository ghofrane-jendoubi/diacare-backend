package tn.esprit.spring.diacarebackend.controller;

import tn.esprit.spring.diacarebackend.entities.*;
import tn.esprit.spring.diacarebackend.repository.*;
import tn.esprit.spring.diacarebackend.Service.ForumModerationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/forum")
@CrossOrigin(origins = "http://localhost:4200")
public class ForumController {

    private final ForumPostRepository postRepo;
    private final ForumCommentRepository commentRepo;
    private final ForumLikeRepository likeRepo;
    private final ForumModerationService moderationService;

    public ForumController(
            ForumPostRepository postRepo,
            ForumCommentRepository commentRepo,
            ForumLikeRepository likeRepo,
            ForumModerationService moderationService) {
        this.postRepo = postRepo;
        this.commentRepo = commentRepo;
        this.likeRepo = likeRepo;
        this.moderationService = moderationService;
    }

    // ===== POSTS =====

    @GetMapping("/posts")
    public ResponseEntity<Page<ForumPost>> getPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                postRepo.findByIsFlaggedFalseOrderByCreatedAtDesc(
                        PageRequest.of(page, size)));
    }

    @GetMapping("/posts/category/{category}")
    public ResponseEntity<Page<ForumPost>> getByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        ForumPost.Category cat = ForumPost.Category.valueOf(category.toUpperCase());
        return ResponseEntity.ok(
                postRepo.findByCategoryAndIsFlaggedFalseOrderByCreatedAtDesc(
                        cat, PageRequest.of(page, size)));
    }

    @GetMapping("/posts/my/{patientId}")
    public ResponseEntity<List<ForumPost>> getMyPosts(@PathVariable Long patientId) {
        return ResponseEntity.ok(postRepo.findByPatientIdOrderByCreatedAtDesc(patientId));
    }

    @PostMapping("/posts")
    @Transactional
    public ResponseEntity<Map<String, Object>> createPost(
            @RequestBody Map<String, Object> body) {

        String content = (String) body.get("content");
        String title = (String) body.get("title");

        // Validation des données requises
        if (content == null || content.trim().isEmpty() || title == null || title.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "blocked", false,
                    "error", "Titre ou contenu vide"
            ));
        }

        Object patientIdObj = body.get("patientId");
        Object patientNameObj = body.get("patientName");
        
        if (patientIdObj == null || patientNameObj == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "blocked", false,
                    "error", "Informations utilisateur manquantes"
            ));
        }

        Long patientId;
        String patientName;
        try {
            patientId = Long.valueOf(patientIdObj.toString());
            patientName = patientNameObj.toString().trim();
            
            if (patientId <= 0 || patientName.isEmpty()) {
                throw new NumberFormatException();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "blocked", false,
                    "error", "ID utilisateur invalide"
            ));
        }

        // Modération IA (désactivée temporairement pour tests)
        Map<String, Object> modResult = Map.of("safe", true, "score", 0.0);
        boolean isSafe = (Boolean) modResult.getOrDefault("safe", true);
        double score = ((Number) modResult.getOrDefault("score", 0.0)).doubleValue();

        // Désactiver temporairement la modération pour tests
        if (false && !isSafe && score > 0.8) {
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "blocked", true,
                    "reason", modResult.getOrDefault("reason", "Contenu non conforme")
            ));
        }

        ForumPost post = new ForumPost();
        post.setPatientId(patientId);
        post.setPatientName(patientName);
        post.setTitle(title);
        post.setContent(content);
        post.setCategory(ForumPost.Category.valueOf(
                body.getOrDefault("category", "EXPERIENCE").toString()));
        post.setIsModerated(!isSafe);
        post.setModerationScore(score);
        if (!isSafe) {
            post.setModerationReason((String) modResult.get("reason"));
        }

        ForumPost saved = postRepo.save(post);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "blocked", false,
                "post", saved,
                "moderation", modResult
        ));
    }

    @DeleteMapping("/posts/{id}")
    @Transactional
    public ResponseEntity<Void> deletePost(
            @PathVariable Long id,
            @RequestParam Long patientId) {
        postRepo.findById(id).ifPresent(post -> {
            if (post.getPatientId().equals(patientId)) {
                postRepo.deleteById(id);
            }
        });
        return ResponseEntity.ok().build();
    }

    // ===== LIKES =====

    @PostMapping("/posts/{id}/like")
    @Transactional
    public ResponseEntity<Map<String, Object>> toggleLike(
            @PathVariable Long id,
            @RequestParam Long patientId) {

        if (likeRepo.existsByPostIdAndPatientId(id, patientId)) {
            likeRepo.deleteByPostIdAndPatientId(id, patientId);
            postRepo.decrementLikeCount(id);
            return ResponseEntity.ok(Map.of("liked", false));
        } else {
            ForumLike like = new ForumLike();
            like.setPostId(id);
            like.setPatientId(patientId);
            likeRepo.save(like);
            postRepo.incrementLikeCount(id);
            return ResponseEntity.ok(Map.of("liked", true));
        }
    }

    // ===== COMMENTAIRES =====

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<List<ForumComment>> getComments(@PathVariable Long postId) {
        return ResponseEntity.ok(commentRepo.findByPostIdOrderByCreatedAtAsc(postId));
    }

    @PostMapping("/posts/{postId}/comments")
    @Transactional
    public ResponseEntity<Map<String, Object>> addComment(
            @PathVariable Long postId,
            @RequestBody Map<String, Object> body) {

        String content = (String) body.get("content");
        
        // Validation des données requises
        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Contenu vide"
            ));
        }
        
        Object patientIdObj = body.get("patientId");
        Object patientNameObj = body.get("patientName");
        
        if (patientIdObj == null || patientNameObj == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Informations utilisateur manquantes"
            ));
        }

        Long patientId;
        String patientName;
        try {
            patientId = Long.valueOf(patientIdObj.toString());
            patientName = patientNameObj.toString().trim();
            
            if (patientId <= 0 || patientName.isEmpty()) {
                throw new NumberFormatException("ID invalide");
            }
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "ID utilisateur invalide"
            ));
        }

        // Modération IA (désactivée temporairement pour tests)
        Map<String, Object> modResult = Map.of("safe", true, "score", 0.0);
        boolean isSafe = (Boolean) modResult.getOrDefault("safe", true);

        // Désactiver temporairement la modération pour tests
        if (false && !isSafe) {
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "blocked", true,
                    "reason", modResult.getOrDefault("reason", "Commentaire non conforme")
            ));
        }

        ForumComment comment = new ForumComment();
        comment.setPostId(postId);
        comment.setPatientId(patientId);
        comment.setPatientName(patientName);
        comment.setContent(content);
        commentRepo.save(comment);
        postRepo.incrementCommentCount(postId);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "blocked", false,
                "comment", comment
        ));
    }
}
