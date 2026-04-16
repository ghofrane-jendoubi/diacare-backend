package tn.esprit.spring.diacarebackend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.spring.diacarebackend.entities.MessageNutrition;
import tn.esprit.spring.diacarebackend.repository.MessageNutritionRepository;

import java.util.List;

@Service
public class MessageNutritionService {

    @Autowired
    private MessageNutritionRepository messageNutritionRepository;

    // ── Envoyer un message ────────────────────────────────────
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
        msg.setRead(false);
        return messageNutritionRepository.save(msg);
    }

    // ── Récupérer conversation d'un patient ───────────────────
    public List<MessageNutrition> getConversation(Long patientId) {
        return messageNutritionRepository.findByPatientIdOrderByCreatedAtAsc(patientId);
    }

    // ── Marquer comme lus ─────────────────────────────────────
    public void markAsRead(Long patientId, Long userId) {
        messageNutritionRepository.markAllAsRead(patientId, userId);
    }

    // ── Compter non lus ───────────────────────────────────────
    public long countUnread(Long receiverId) {
        return messageNutritionRepository.countByReceiverIdAndIsReadFalse(receiverId);
    }
}