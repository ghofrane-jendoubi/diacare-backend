// MessageNutritionController.java
package tn.esprit.spring.diacarebackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.spring.diacarebackend.entities.MessageNutrition;
import tn.esprit.spring.diacarebackend.services.MessageNutritionService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/message")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class MessageNutritionController {

    @Autowired
    private MessageNutritionService messageNutritionService;

    @GetMapping("/conversation/{patientId}")
    public ResponseEntity<List<MessageNutrition>> getConversation(@PathVariable Long patientId) {
        return ResponseEntity.ok(messageNutritionService.getConversation(patientId));
    }

    // ✅ NOUVEAU - Conversations du nutritionniste
    @GetMapping("/nutritionist/{nutritionistId}/conversations")
    public ResponseEntity<List<Map<String, Object>>> getNutritionistConversations(@PathVariable Long nutritionistId) {
        return ResponseEntity.ok(messageNutritionService.getNutritionistConversations(nutritionistId));
    }

    // ✅ NOUVEAU - Messages entre nutritionniste et patient
    @GetMapping("/conversation/{nutritionistId}/{patientId}")
    public ResponseEntity<List<MessageNutrition>> getConversationBetween(
            @PathVariable Long nutritionistId,
            @PathVariable Long patientId) {
        return ResponseEntity.ok(messageNutritionService.getConversationBetween(nutritionistId, patientId));
    }

    @PostMapping("/send")
    public ResponseEntity<MessageNutrition> sendMessage(@RequestBody Map<String, Object> body) {
        Long senderId = Long.valueOf(body.get("senderId").toString());
        String senderRole = body.get("senderRole").toString();
        Long receiverId = Long.valueOf(body.get("receiverId").toString());
        String receiverRole = body.get("receiverRole").toString();
        Long patientId = Long.valueOf(body.get("patientId").toString());
        String content = body.get("content").toString();

        MessageNutrition msg = messageNutritionService.sendMessage(
                senderId, senderRole, receiverId, receiverRole, patientId, content
        );
        return ResponseEntity.ok(msg);
    }

    @PutMapping("/read/{patientId}/{userId}")
    public ResponseEntity<Void> markAsRead(@PathVariable Long patientId,
                                           @PathVariable Long userId) {
        messageNutritionService.markAsRead(patientId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/unread/{receiverId}")
    public ResponseEntity<Long> countUnread(@PathVariable Long receiverId) {
        return ResponseEntity.ok(messageNutritionService.countUnread(receiverId));
    }
}