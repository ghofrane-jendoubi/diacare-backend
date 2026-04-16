package tn.esprit.spring.diacarebackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.spring.diacarebackend.entities.MessageNutrition;
import tn.esprit.spring.diacarebackend.services.MessageNutritionService;
import tn.esprit.spring.diacarebackend.services.MessageNutritionService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/message")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class MessageNutritionController {

    @Autowired
    private MessageNutritionService messageNutritionService;

    // ── GET conversation patient ──────────────────────────────
    // Appelé par nutritionniste ET patient
    @GetMapping("/conversation/{patientId}")
    public ResponseEntity<List<MessageNutrition>> getConversation(@PathVariable Long patientId) {
        return ResponseEntity.ok(messageNutritionService.getConversation(patientId));
    }

    // ── POST envoyer message ──────────────────────────────────
    // Body : { senderId, senderRole, receiverId, receiverRole, patientId, content }
    @PostMapping("/send")
    public ResponseEntity<MessageNutrition> sendMessage(@RequestBody Map<String, Object> body) {
        Long   senderId     = Long.valueOf(body.get("senderId").toString());
        String senderRole   = body.get("senderRole").toString();
        Long   receiverId   = Long.valueOf(body.get("receiverId").toString());
        String receiverRole = body.get("receiverRole").toString();
        Long   patientId    = Long.valueOf(body.get("patientId").toString());
        String content      = body.get("content").toString();

        MessageNutrition msg = messageNutritionService.sendMessage(
                senderId, senderRole, receiverId, receiverRole, patientId, content
        );
        return ResponseEntity.ok(msg);
    }

    // ── PUT marquer comme lus ─────────────────────────────────
    @PutMapping("/read/{patientId}/{userId}")
    public ResponseEntity<Void> markAsRead(@PathVariable Long patientId,
                                           @PathVariable Long userId) {
        messageNutritionService.markAsRead(patientId, userId);
        return ResponseEntity.ok().build();
    }

    // ── GET compter non lus ───────────────────────────────────
    @GetMapping("/unread/{receiverId}")
    public ResponseEntity<Long> countUnread(@PathVariable Long receiverId) {
        return ResponseEntity.ok(messageNutritionService.countUnread(receiverId));
    }
}