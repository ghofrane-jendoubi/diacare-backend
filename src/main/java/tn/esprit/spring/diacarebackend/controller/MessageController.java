package tn.esprit.spring.diacarebackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.spring.diacarebackend.dto.ConversationDTO;
import tn.esprit.spring.diacarebackend.dto.DoctorConversationDTO;
import tn.esprit.spring.diacarebackend.dto.MessageRequest;
import tn.esprit.spring.diacarebackend.entities.Message;
import tn.esprit.spring.diacarebackend.entities.User;
import tn.esprit.spring.diacarebackend.repository.MessageRepository;
import tn.esprit.spring.diacarebackend.repository.PatientRepository;
import tn.esprit.spring.diacarebackend.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private PatientRepository patientRepository;

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
        message.setDocumentUrl(request.getDocumentUrl());
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

    @PostMapping("/mark-read-between")
    public ResponseEntity<Void> markMessagesAsRead(
            @RequestParam Long doctorId,
            @RequestParam Long patientId) {

        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        User patient = userRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        messageRepository.markMessagesAsReadBetween(doctor, patient);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/conversations/patient/{patientId}")
    public ResponseEntity<List<DoctorConversationDTO>> getPatientConversations(@PathVariable Long patientId) {
        patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        List<Object[]> results = messageRepository.findDoctorConversationsByPatientNative(patientId);
        List<DoctorConversationDTO> conversations = new ArrayList<>();

        for (Object[] row : results) {
            DoctorConversationDTO dto = new DoctorConversationDTO(
                    ((Number) row[0]).longValue(),  // doctorId
                    (String) row[1],                // doctorName
                    (String) row[2],                // doctorProfilePicture
                    (String) row[3],                // speciality
                    (String) row[4],                // lastMessage
                    row[5] != null ? LocalDateTime.parse(row[5].toString().replace(" ", "T")) : null,
                    row[6] != null ? ((Number) row[6]).longValue() : null,
                    row[7] != null ? ((Number) row[7]).longValue() : 0L
            );
            conversations.add(dto);
        }

        return ResponseEntity.ok(conversations);
    }

    @GetMapping("/conversations/doctor/{doctorId}")
    public ResponseEntity<List<ConversationDTO>> getDoctorConversations(@PathVariable Long doctorId) {
        userRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        List<Object[]> results = messageRepository.findConversationsByDoctorNative(doctorId);
        List<ConversationDTO> conversations = new ArrayList<>();

        for (Object[] row : results) {
            ConversationDTO dto = new ConversationDTO(
                    ((Number) row[0]).longValue(),
                    (String) row[1],
                    (String) row[2],
                    (String) row[3],
                    (String) row[4],
                    row[5] != null ? LocalDateTime.parse((CharSequence) row[5].toString().replace(" ", "T")) : null,
                    row[6] != null ? ((Number) row[6]).longValue() : null,
                    row[7] != null ? ((Number) row[7]).longValue() : 0L
            );
            conversations.add(dto);
        }

        return ResponseEntity.ok(conversations);
    }
}

