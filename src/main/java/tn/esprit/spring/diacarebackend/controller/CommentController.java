package tn.esprit.spring.diacarebackend.controller;

import tn.esprit.spring.diacarebackend.DTOs.CommentDTO;
import tn.esprit.spring.diacarebackend.entities.ContentComment;
import tn.esprit.spring.diacarebackend.repository.ContentCommentRepository;
import tn.esprit.spring.diacarebackend.repository.EducationalContentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/education")
@CrossOrigin(origins = "http://localhost:4200")
public class CommentController {

    private final ContentCommentRepository commentRepo;
    private final EducationalContentRepository contentRepo;

    public CommentController(ContentCommentRepository commentRepo,
                             EducationalContentRepository contentRepo) {
        this.commentRepo = commentRepo;
        this.contentRepo = contentRepo;
    }

    @GetMapping("/contents/{contentId}/comments")
    public ResponseEntity<List<CommentDTO>> getComments(@PathVariable Long contentId) {
        List<ContentComment> comments = commentRepo
                .findByContentIdAndParentCommentIdIsNullAndIsApprovedTrueOrderByCreatedAtDesc(contentId);
        List<CommentDTO> dtos = comments.stream().map(c -> {
            CommentDTO dto = toDTO(c);
            dto.setReplies(
                    commentRepo.findByParentCommentIdAndIsApprovedTrue(c.getId())
                            .stream().map(this::toDTO).collect(Collectors.toList())
            );
            return dto;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/contents/{contentId}/comments")
    @Transactional
    public ResponseEntity<CommentDTO> addComment(
            @PathVariable Long contentId,
            @RequestBody Map<String, Object> body) {

        try {
            System.out.println("=== Reçu commentaire pour contentId: " + contentId);
            System.out.println("=== Body: " + body);

            ContentComment comment = new ContentComment();
            comment.setContentId(contentId);
            comment.setUserId(1L);
            comment.setUserName(body.get("userName") != null ?
                    (String) body.get("userName") : "Patient");
            comment.setCommentText((String) body.get("commentText"));

            if (body.get("parentCommentId") != null) {
                comment.setParentCommentId(
                        Long.valueOf(body.get("parentCommentId").toString()));
            }

            ContentComment saved = commentRepo.save(comment);
            System.out.println("=== Commentaire sauvegardé id: " + saved.getId());

            try {
                contentRepo.incrementCommentCount(contentId);
            } catch (Exception e) {
                System.err.println("Erreur incrementCommentCount: " + e.getMessage());
            }

            return ResponseEntity.ok(toDTO(saved));

        } catch (Exception e) {
            System.err.println("=== ERREUR addComment: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    private CommentDTO toDTO(ContentComment c) {
        CommentDTO dto = new CommentDTO();
        dto.setId(c.getId());
        dto.setContentId(c.getContentId());
        dto.setUserName(c.getUserName());
        dto.setUserAvatar(c.getUserAvatar());
        dto.setCommentText(c.getCommentText());
        dto.setParentCommentId(c.getParentCommentId());
        dto.setLikeCount(c.getLikeCount());
        dto.setCreatedAt(c.getCreatedAt());
        return dto;
    }
}