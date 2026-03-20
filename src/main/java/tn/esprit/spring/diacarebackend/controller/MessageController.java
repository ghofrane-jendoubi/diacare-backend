package tn.esprit.spring.diacarebackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.spring.diacarebackend.dto.ConversationDTO;
import tn.esprit.spring.diacarebackend.entities.Message;
import tn.esprit.spring.diacarebackend.entities.User;
import tn.esprit.spring.diacarebackend.repository.MessageRepository;
import tn.esprit.spring.diacarebackend.repository.PatientRepository;
import tn.esprit.spring.diacarebackend.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "http://localhost:4200")
public class MessageController {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PatientRepository patientRepository;  // ← Ajouté

    @GetMapping("/conversation")
    public ResponseEntity<List<Message>> getConversation(
            @RequestParam Long userId1,
            @RequestParam Long userId2) {

        User user1 = userRepository.findById(userId1)
                .orElseThrow(() -> new RuntimeException("User not found"));
        User user2 = userRepository.findById(userId2)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Message> conversation = messageRepository.findConversation(user1, user2);
        return ResponseEntity.ok(conversation);
    }

    @PostMapping("/send")
    public ResponseEntity<Message> sendMessage(@RequestBody MessageRequest request) {
        User sender = userRepository.findById(request.getSenderId())
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        User receiver = userRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        Message message = new Message();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(request.getContent() != null ? request.getContent() : "");
        message.setImageUrl(request.getImageUrl());
        message.setAudioUrl(request.getAudioUrl());
        message.setAudioDuration(request.getAudioDuration());
        message.setSentAt(LocalDateTime.now());

        Message saved = messageRepository.save(message);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/mark-read")
    public ResponseEntity<Void> markAsRead(@RequestParam Long receiverId) {
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        messageRepository.markAllAsRead(receiver);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/conversations/patient/{patientId}")
    public ResponseEntity<List<ConversationDTO>> getPatientConversations(@PathVariable Long patientId) {
        // Vérifier que le patient existe
        patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        List<ConversationDTO> conversations = messageRepository.findConversationsByPatient(patientId);
        return ResponseEntity.ok(conversations);
    }
}

// DTO pour les requêtes
class MessageRequest {
    private Long senderId;
    private Long receiverId;
    private String content;
    private String imageUrl;
    private String audioUrl;
    private Integer audioDuration;

    // Getters et setters...
    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }

    public Long getReceiverId() { return receiverId; }
    public void setReceiverId(Long receiverId) { this.receiverId = receiverId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getAudioUrl() { return audioUrl; }
    public void setAudioUrl(String audioUrl) { this.audioUrl = audioUrl; }

    public Integer getAudioDuration() { return audioDuration; }
    public void setAudioDuration(Integer audioDuration) { this.audioDuration = audioDuration; }
}