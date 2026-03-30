package tn.esprit.spring.diacarebackend.controller;

import org.springframework.transaction.annotation.Transactional;
import tn.esprit.spring.diacarebackend.DTOs.PrivateMessageDTO;
import tn.esprit.spring.diacarebackend.entities.PrivateMessage;
import tn.esprit.spring.diacarebackend.repository.PrivateMessageRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/patient")
@CrossOrigin(origins = "http://localhost:4200")
public class PatientMessagingController {

    private final PrivateMessageRepository messageRepo;

    public PatientMessagingController(PrivateMessageRepository messageRepo) {
        this.messageRepo = messageRepo;
    }

    // ===== MESSAGERIE PATIENT =====

    // Récupérer tous les messages d'un patient (envoyés + reçus)
    @GetMapping("/messages/{patientId}")
    public ResponseEntity<Map<String, Object>> getAllPatientMessages(@PathVariable Long patientId) {
        List<PrivateMessageDTO> sent = messageRepo.findBySenderIdOrderByCreatedAtDesc(patientId)
                .stream().map(this::toMessageDTO).collect(Collectors.toList());
        
        List<PrivateMessageDTO> received = messageRepo.findByReceiverIdOrderByCreatedAtDesc(patientId)
                .stream().map(this::toMessageDTO).collect(Collectors.toList());
        
        return ResponseEntity.ok(Map.of(
                "sent", sent,
                "received", received,
                "unreadCount", received.stream().filter(m -> !m.getIsRead()).count()
        ));
    }

    // Récupérer les messages reçus par le patient
    @GetMapping("/messages/received/{patientId}")
    public ResponseEntity<List<PrivateMessageDTO>> getPatientReceivedMessages(@PathVariable Long patientId) {
        return ResponseEntity.ok(
                messageRepo.findByReceiverIdOrderByCreatedAtDesc(patientId)
                        .stream().map(this::toMessageDTO).collect(Collectors.toList())
        );
    }

    // Récupérer les messages envoyés par le patient
    @GetMapping("/messages/sent/{patientId}")
    public ResponseEntity<List<PrivateMessageDTO>> getPatientSentMessages(@PathVariable Long patientId) {
        return ResponseEntity.ok(
                messageRepo.findBySenderIdOrderByCreatedAtDesc(patientId)
                        .stream().map(this::toMessageDTO).collect(Collectors.toList())
        );
    }

    // Envoyer un message du patient vers un médecin
    @PostMapping("/messages/send")
    @Transactional
    public ResponseEntity<PrivateMessageDTO> sendPatientMessage(
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

    // Marquer message comme lu
    @PatchMapping("/messages/{messageId}/read")
    @Transactional
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<Void> markMessageAsRead(@PathVariable Long messageId) {
        messageRepo.findById(messageId).ifPresent(msg -> {
            msg.setIsRead(true);
            messageRepo.save(msg);
        });
        return ResponseEntity.ok().build();
    }

    // Supprimer un message
    @DeleteMapping("/messages/{messageId}")
    @Transactional
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long messageId) {
        messageRepo.findById(messageId).ifPresent(msg -> {
            messageRepo.delete(msg);
        });
        return ResponseEntity.ok().build();
    }

    // ===== COMMENTAIRES PATIENT =====

    // Ajouter un commentaire en tant que patient
    @PostMapping("/comments/{contentId}")
    @Transactional
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<Map<String, Object>> addPatientComment(
            @PathVariable Long contentId,
            @RequestBody Map<String, Object> body) {
        
        try {
            String commentText = (String) body.get("commentText");
            String userName = (String) body.get("userName");
            Long parentCommentId = body.get("parentCommentId") != null ? 
                Long.valueOf(body.get("parentCommentId").toString()) : null;
            
            if (commentText == null || commentText.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            // Créer un commentaire privé pour le patient (différent des commentaires publics)
            PrivateMessage comment = new PrivateMessage();
            comment.setContentId(contentId);
            comment.setMessage(commentText);
            comment.setSenderName(userName);
            comment.setSenderId(1L); // ID patient par défaut
            comment.setIsRead(true); // Les commentaires patients sont auto-lus
            
            PrivateMessage saved = messageRepo.save(comment);
            return ResponseEntity.ok(Map.of("success", true, "comment", toMessageDTO(saved)));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // Récupérer les réponses des médecins aux messages du patient
    @GetMapping("/doctor-replies/{patientId}")
    public ResponseEntity<List<PrivateMessageDTO>> getDoctorReplies(@PathVariable Long patientId) {
        return ResponseEntity.ok(
                messageRepo.findByReceiverIdOrderByCreatedAtDesc(patientId)
                        .stream()
                        .filter(msg -> msg.getSenderId() != null && !msg.getSenderId().equals(patientId))
                        .map(this::toMessageDTO)
                        .collect(Collectors.toList())
        );
    }

    // ===== HELPER =====

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
    }
}
