package tn.esprit.spring.diacarebackend.controller;

import tn.esprit.spring.diacarebackend.DTOs.*;
import tn.esprit.spring.diacarebackend.entities.*;
import tn.esprit.spring.diacarebackend.Service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import tn.esprit.spring.diacarebackend.repository.ContentCommentRepository;
import tn.esprit.spring.diacarebackend.repository.EducationalContentRepository;
import tn.esprit.spring.diacarebackend.repository.PrivateMessageRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/doctor/education")
@CrossOrigin(origins = "http://localhost:4200")
public class DoctorEducationController {

    private final EducationalContentRepository contentRepo;
    private final ContentCommentRepository commentRepo;
    private final PrivateMessageRepository messageRepo;
    private final NotificationService notificationService;

    public DoctorEducationController(
            EducationalContentRepository contentRepo,
            ContentCommentRepository commentRepo,
            PrivateMessageRepository messageRepo,
            NotificationService notificationService) {
        this.contentRepo = contentRepo;
        this.commentRepo = commentRepo;
        this.messageRepo = messageRepo;
        this.notificationService = notificationService;
    }

    // ===== ARTICLES =====

    // Récupérer tous les articles du médecin
    @GetMapping("/contents/doctor/{doctorId}")
    public ResponseEntity<List<ContentSummaryDTO>> getDoctorContents(
            @PathVariable Long doctorId) {
        List<EducationalContent> contents = contentRepo.findByAuthorIdOrderByCreatedAtDesc(doctorId);
        return ResponseEntity.ok(contents.stream().map(this::toSummaryDTO).collect(Collectors.toList()));
    }

    // Créer un article
    @PostMapping("/contents")
    @Transactional
    public ResponseEntity<ContentSummaryDTO> createContent(
            @RequestBody ArticleCreateDTO dto,
            @RequestParam(defaultValue = "1") Long doctorId,
            @RequestParam(defaultValue = "Dr. Médecin") String doctorName) {

        EducationalContent content = new EducationalContent();
        content.setTitle(dto.getTitle());
        content.setSubtitle(dto.getSubtitle());
        content.setContent(dto.getContent());
        content.setSummary(dto.getSummary());
        content.setCategory(EducationalContent.Category.valueOf(dto.getCategory()));
        content.setContentType(EducationalContent.ContentType.valueOf(dto.getContentType()));
        content.setThumbnailUrl(dto.getThumbnailUrl());
        content.setVideoUrl(dto.getVideoUrl());
        content.setTags(dto.getTags());
        content.setReadingTime(dto.getReadingTime() != null ? dto.getReadingTime() : 5);
        content.setDifficultyLevel(EducationalContent.DifficultyLevel.valueOf(
                dto.getDifficultyLevel() != null ? dto.getDifficultyLevel() : "BEGINNER"));
        content.setIsFeatured(dto.getIsFeatured() != null ? dto.getIsFeatured() : false);
        content.setIsPublished(dto.getIsPublished() != null ? dto.getIsPublished() : true);
        content.setAuthorId(doctorId);
        content.setAuthorName(doctorName);

        EducationalContent saved = contentRepo.save(content);
        return ResponseEntity.ok(toSummaryDTO(saved));
    }

    // Modifier un article
    @PutMapping("/contents/{id}")
    @Transactional
    public ResponseEntity<ContentSummaryDTO> updateContent(
            @PathVariable Long id,
            @RequestBody ArticleCreateDTO dto) {

        EducationalContent content = contentRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Article non trouvé"));

        if (dto.getTitle() != null) content.setTitle(dto.getTitle());
        if (dto.getSubtitle() != null) content.setSubtitle(dto.getSubtitle());
        if (dto.getContent() != null) content.setContent(dto.getContent());
        if (dto.getSummary() != null) content.setSummary(dto.getSummary());
        if (dto.getCategory() != null) content.setCategory(
                EducationalContent.Category.valueOf(dto.getCategory()));
        if (dto.getThumbnailUrl() != null) content.setThumbnailUrl(dto.getThumbnailUrl());
        if (dto.getVideoUrl() != null) content.setVideoUrl(dto.getVideoUrl());
        if (dto.getTags() != null) content.setTags(dto.getTags());
        if (dto.getReadingTime() != null) content.setReadingTime(dto.getReadingTime());
        if (dto.getIsPublished() != null) content.setIsPublished(dto.getIsPublished());
        if (dto.getIsFeatured() != null) content.setIsFeatured(dto.getIsFeatured());

        return ResponseEntity.ok(toSummaryDTO(contentRepo.save(content)));
    }

    // Supprimer un article
    @DeleteMapping("/contents/{id}")
    @Transactional
    public ResponseEntity<Void> deleteContent(@PathVariable Long id) {
        contentRepo.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // Publier/Dépublier un article
    @PatchMapping("/contents/{id}/toggle-publish")
    @Transactional
    public ResponseEntity<Map<String, Object>> togglePublish(@PathVariable Long id) {
        EducationalContent content = contentRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Article non trouvé"));
        content.setIsPublished(!content.getIsPublished());
        contentRepo.save(content);
        return ResponseEntity.ok(Map.of("isPublished", content.getIsPublished()));
    }

    // ===== STATISTIQUES =====

    @GetMapping("/stats/doctor/{doctorId}")
    public ResponseEntity<Map<String, Object>> getDoctorStats(@PathVariable Long doctorId) {
        List<EducationalContent> contents = contentRepo.findByAuthorIdOrderByCreatedAtDesc(doctorId);

        long totalViews = contents.stream().mapToLong(c -> c.getViewCount() != null ? c.getViewCount() : 0).sum();
        long totalLikes = contents.stream().mapToLong(c -> c.getLikeCount() != null ? c.getLikeCount() : 0).sum();
        long totalComments = contents.stream().mapToLong(c -> c.getCommentCount() != null ? c.getCommentCount() : 0).sum();
        long totalArticles = contents.size();
        long unreadMessages = messageRepo.countByReceiverIdAndIsReadFalse(doctorId);

        return ResponseEntity.ok(Map.of(
                "totalArticles", totalArticles,
                "totalViews", totalViews,
                "totalLikes", totalLikes,
                "totalComments", totalComments,
                "unreadMessages", unreadMessages
        ));
    }

    // ===== COMMENTAIRES =====

    // Tous les commentaires des articles du médecin
    @GetMapping("/comments/doctor/{doctorId}")
    public ResponseEntity<List<CommentDTO>> getDoctorComments(@PathVariable Long doctorId) {
        List<EducationalContent> contents = contentRepo.findByAuthorIdOrderByCreatedAtDesc(doctorId);
        List<CommentDTO> allComments = contents.stream()
                .flatMap(c -> commentRepo
                        .findByContentIdAndParentCommentIdIsNullAndIsApprovedTrueOrderByCreatedAtDesc(c.getId())
                        .stream().map(comment -> {
                            CommentDTO dto = toCommentDTO(comment);
                            dto.setReplies(
                                    commentRepo.findByParentCommentIdAndIsApprovedTrue(comment.getId())
                                            .stream().map(this::toCommentDTO).collect(Collectors.toList())
                            );
                            return dto;
                        }))
                .collect(Collectors.toList());
        return ResponseEntity.ok(allComments);
    }

    // Répondre publiquement à un commentaire
    @PostMapping("/comments/{commentId}/reply")
    @Transactional
    public ResponseEntity<CommentDTO> replyToComment(
            @PathVariable Long commentId,
            @RequestBody Map<String, Object> body) {

        ContentComment parent = commentRepo.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Commentaire non trouvé"));

        ContentComment reply = new ContentComment();
        reply.setContentId(parent.getContentId());
        reply.setUserId(Long.valueOf(body.get("doctorId").toString()));
        reply.setUserName((String) body.get("doctorName"));
        reply.setCommentText((String) body.get("replyText"));
        reply.setParentCommentId(commentId);
        reply.setIsApproved(true);

        ContentComment saved = commentRepo.save(reply);
        contentRepo.incrementCommentCount(parent.getContentId());
        return ResponseEntity.ok(toCommentDTO(saved));
    }

    // Supprimer un commentaire inapproprié
    @DeleteMapping("/comments/{commentId}")
    @Transactional
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        commentRepo.deleteById(commentId);
        return ResponseEntity.ok().build();
    }

    // ===== MESSAGES PRIVÉS =====

    // Envoyer un message privé
    @PostMapping("/messages/send")
    @Transactional
    public ResponseEntity<PrivateMessageDTO> sendMessage(
            @RequestBody Map<String, Object> body) {

        // Validate required fields
        if (body.get("senderId") == null) {
            return ResponseEntity.badRequest().build();
        }
        if (body.get("receiverId") == null) {
            return ResponseEntity.badRequest().build();
        }
        if (body.get("message") == null || body.get("message").toString().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        PrivateMessage msg = new PrivateMessage();
        msg.setSenderId(Long.valueOf(body.get("senderId").toString()));
        msg.setReceiverId(Long.valueOf(body.get("receiverId").toString()));
        msg.setSenderName((String) body.get("senderName"));
        msg.setReceiverName((String) body.get("receiverName"));
        msg.setMessage((String) body.get("message"));
        if (body.get("contentId") != null) msg.setContentId(Long.valueOf(body.get("contentId").toString()));
        if (body.get("commentId") != null) msg.setCommentId(Long.valueOf(body.get("commentId").toString()));

        PrivateMessage saved = messageRepo.save(msg);
        return ResponseEntity.ok(toMessageDTO(saved));
    }

    // Récupérer les messages reçus
    @GetMapping("/messages/received/{userId}")
    public ResponseEntity<List<PrivateMessageDTO>> getReceivedMessages(@PathVariable Long userId) {
        return ResponseEntity.ok(
                messageRepo.findByReceiverIdOrderByCreatedAtDesc(userId)
                        .stream().map(this::toMessageDTO).collect(Collectors.toList())
        );
    }

    // Récupérer les messages envoyés par un utilisateur (patient)
    @GetMapping("/messages/sent/{userId}")
    public ResponseEntity<List<PrivateMessageDTO>> getSentMessages(@PathVariable Long userId) {
        return ResponseEntity.ok(
                messageRepo.findBySenderIdOrderByCreatedAtDesc(userId)
                        .stream().map(this::toMessageDTO).collect(Collectors.toList())
        );
    }

    // Récupérer tous les messages d'un utilisateur (envoyés + reçus)
    @GetMapping("/messages/all/{userId}")
    public ResponseEntity<Map<String, Object>> getAllMessages(@PathVariable Long userId) {
        List<PrivateMessageDTO> sent = messageRepo.findBySenderIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toMessageDTO).collect(Collectors.toList());

        List<PrivateMessageDTO> received = messageRepo.findByReceiverIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toMessageDTO).collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
                "sent", sent,
                "received", received
        ));
    } // Added closing bracket here

    @PatchMapping("/messages/{messageId}/read")
    @Transactional
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<Void> markMessageAsRead(@PathVariable Long messageId) {
        messageRepo.findById(messageId).ifPresent(msg -> {
            msg.setIsRead(true);
            messageRepo.save(msg);
        });
        return ResponseEntity.ok().build(); // Added return statement here
    }

    // Supprimer un message (pour les médecins et patients)
    @DeleteMapping("/messages/{messageId}")
    @Transactional
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long messageId) {
        messageRepo.findById(messageId).ifPresent(msg -> {
            messageRepo.delete(msg);
        });
        return ResponseEntity.ok().build();
    }

    // ===== NOTIFICATIONS =====

    @GetMapping("/notifications/doctor/{doctorId}")
    public ResponseEntity<List<Notification>> getNotifications(@PathVariable Long doctorId) {
        return ResponseEntity.ok(notificationService.getNotificationsForDoctor(doctorId));
    }

    @GetMapping("/notifications/doctor/{doctorId}/unread")
    public ResponseEntity<List<Notification>> getUnreadNotifications(@PathVariable Long doctorId) {
        return ResponseEntity.ok(notificationService.getUnreadNotificationsForDoctor(doctorId));
    }

    @GetMapping("/notifications/doctor/{doctorId}/unread-count")
    public ResponseEntity<Long> getUnreadCount(@PathVariable Long doctorId) {
        return ResponseEntity.ok(notificationService.getUnreadCountForDoctor(doctorId));
    }

    @PatchMapping("/notifications/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/notifications/doctor/{doctorId}/read-all")
    public ResponseEntity<Void> markAllAsRead(@PathVariable Long doctorId) {
        notificationService.markAllAsReadForDoctor(doctorId);
        return ResponseEntity.ok().build();
    }

    private ContentSummaryDTO toSummaryDTO(EducationalContent c) {
        ContentSummaryDTO dto = new ContentSummaryDTO();
        dto.setId(c.getId());
        dto.setTitle(c.getTitle());
        dto.setSummary(c.getSummary());
        dto.setCategory(c.getCategory() != null ? c.getCategory().name() : "");
        dto.setContentType(c.getContentType() != null ? c.getContentType().name() : "ARTICLE");
        dto.setThumbnailUrl(c.getThumbnailUrl());
        dto.setAuthorName(c.getAuthorName());
        dto.setViewCount(c.getViewCount());
        dto.setLikeCount(c.getLikeCount());
        dto.setCommentCount(c.getCommentCount());
        dto.setReadingTime(c.getReadingTime());
        dto.setDifficultyLevel(c.getDifficultyLevel() != null ? c.getDifficultyLevel().name() : "BEGINNER");
        dto.setIsFeatured(c.getIsFeatured());
        dto.setTags(c.getTags());
        dto.setCreatedAt(c.getCreatedAt());
        dto.setIsPublished(c.getIsPublished());
        return dto;
    }

    private CommentDTO toCommentDTO(ContentComment c) {
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

    private PrivateMessageDTO toMessageDTO(PrivateMessage m) {
        PrivateMessageDTO dto = new PrivateMessageDTO();
        dto.setId(m.getId());
        dto.setSenderId(m.getSenderId());
        dto.setReceiverId(m.getReceiverId());
        dto.setSenderName(m.getSenderName());
        dto.setReceiverName(m.getReceiverName());
        dto.setContentId(m.getContentId());
        dto.setCommentId(m.getCommentId());
        dto.setMessage(m.getMessage());
        dto.setIsRead(m.getIsRead());
        dto.setCreatedAt(m.getCreatedAt());
        return dto;
    }}