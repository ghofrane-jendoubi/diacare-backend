package tn.esprit.spring.diacarebackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.spring.diacarebackend.entities.MessageNutrition;

import java.util.List;

public interface MessageNutritionRepository extends JpaRepository<MessageNutrition, Long> {

    // Tous les messages d'une conversation patient ↔ nutritionniste
    List<MessageNutrition> findByPatientIdOrderByCreatedAtAsc(Long patientId);

    // Messages non lus pour un destinataire
    List<MessageNutrition> findByReceiverIdAndIsReadFalse(Long receiverId);

    // Compter non lus
    long countByReceiverIdAndIsReadFalse(Long receiverId);

    // Marquer tous comme lus pour un patient donné
    @Modifying
    @Transactional
    @Query("UPDATE MessageNutrition m SET m.isRead = true WHERE m.patientId = :patientId AND m.receiverId = :userId")
    void markAllAsRead(Long patientId, Long userId);
}