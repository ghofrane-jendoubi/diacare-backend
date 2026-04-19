package tn.esprit.spring.diacarebackend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.spring.diacarebackend.entities.MessageNutrition;
import tn.esprit.spring.diacarebackend.repository.MessageNutritionRepository;
import tn.esprit.spring.diacarebackend.repository.PatientRepository;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class MessageNutritionService {

    @Autowired
    private MessageNutritionRepository messageNutritionRepository;

    @Autowired
    private PatientRepository patientRepository;

    // Récupérer la conversation d'un patient (tous les messages où patientId = id)
    public List<MessageNutrition> getConversation(Long patientId) {
        return messageNutritionRepository.findByPatientIdOrderByCreatedAtAsc(patientId);
    }

    // Marquer tous les messages comme lus pour un patient et un destinataire
    public void markAllAsRead(Long patientId, Long receiverId) {
        List<MessageNutrition> messages = messageNutritionRepository.findByPatientIdAndReceiverId(patientId, receiverId);
        for (MessageNutrition msg : messages) {
            if (!msg.isRead()) {
                msg.setRead(true);
                messageNutritionRepository.save(msg);
            }
        }
    }

    // Envoyer un message
    public MessageNutrition sendMessage(Long senderId, String senderRole,
                                        Long receiverId, String receiverRole,
                                        Long patientId, String content) {
        MessageNutrition msg = new MessageNutrition();
        msg.setSenderId(senderId);
        msg.setSenderRole(senderRole);
        msg.setReceiverId(receiverId);
        msg.setReceiverRole(receiverRole);
        msg.setPatientId(patientId);
        msg.setContent(content);
        msg.setCreatedAt(LocalDateTime.now());
        msg.setRead(false);

        return messageNutritionRepository.save(msg);
    }

    // Marquer comme lu (pour un message spécifique ou tous)
    public void markAsRead(Long patientId, Long userId) {
        List<MessageNutrition> messages = messageNutritionRepository.findByPatientIdAndReceiverId(patientId, userId);
        for (MessageNutrition msg : messages) {
            if (!msg.isRead()) {
                msg.setRead(true);
                messageNutritionRepository.save(msg);
            }
        }
    }

    // Compter les messages non lus
    public long countUnread(Long receiverId) {
        return messageNutritionRepository.countByReceiverIdAndIsReadFalse(receiverId);
    }

    // MessageNutritionService.java - Partie corrigée
    public List<Map<String, Object>> getNutritionistConversations(Long nutritionistId) {
        List<MessageNutrition> messages = messageNutritionRepository.findMessagesByUserId(nutritionistId);

        Map<Long, Map<String, Object>> conversationsMap = new LinkedHashMap<>();

        for (MessageNutrition msg : messages) {
            Long patientId = msg.getPatientId();

            if (!conversationsMap.containsKey(patientId)) {
                Map<String, Object> conv = new HashMap<>();
                conv.put("patientId", patientId);
                conv.put("patientFirstName", getPatientFirstName(patientId));
                conv.put("patientLastName", getPatientLastName(patientId));
                conv.put("lastMessage", msg.getContent());
                conv.put("lastMessageTime", msg.getCreatedAt());
                conv.put("unreadCount", msg.isRead() ? 0 : 1);
                conversationsMap.put(patientId, conv);
            } else {
                Map<String, Object> existing = conversationsMap.get(patientId);
                if (msg.getCreatedAt().compareTo((LocalDateTime) existing.get("lastMessageTime")) > 0) {
                    existing.put("lastMessage", msg.getContent());
                    existing.put("lastMessageTime", msg.getCreatedAt());
                }
                if (!msg.isRead()) {
                    int currentUnread = (int) existing.get("unreadCount");
                    existing.put("unreadCount", currentUnread + 1);
                }
            }
        }

        return new ArrayList<>(conversationsMap.values());
    }

    // ✅ NOUVEAU - Récupérer les messages entre nutritionniste et patient
    public List<MessageNutrition> getConversationBetween(Long nutritionistId, Long patientId) {
        return messageNutritionRepository.findConversationBetween(nutritionistId, patientId);
    }

    // Helper pour récupérer le prénom du patient
    private String getPatientFirstName(Long patientId) {
        return patientRepository.findById(patientId)
                .map(patient -> patient.getFirstName())
                .orElse("Patient");
    }

    // Helper pour récupérer le nom du patient
    private String getPatientLastName(Long patientId) {
        return patientRepository.findById(patientId)
                .map(patient -> patient.getLastName())
                .orElse("");
    }
}