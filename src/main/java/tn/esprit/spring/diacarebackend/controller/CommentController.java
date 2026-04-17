package tn.esprit.spring.diacarebackend.controller;

import tn.esprit.spring.diacarebackend.dto.CommentDTO;
import tn.esprit.spring.diacarebackend.entities.ContentComment;
import tn.esprit.spring.diacarebackend.repository.ContentCommentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/education")
@CrossOrigin(origins = "http://localhost:4200")
public class CommentController {

    private final ContentCommentRepository commentRepo;

    public CommentController(ContentCommentRepository commentRepo) {
        this.commentRepo = commentRepo;
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
