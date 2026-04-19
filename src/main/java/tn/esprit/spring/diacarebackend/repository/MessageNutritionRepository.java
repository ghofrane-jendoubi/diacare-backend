// MessageNutritionRepository.java
package tn.esprit.spring.diacarebackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.spring.diacarebackend.entities.MessageNutrition;

import java.util.List;

@Repository
public interface MessageNutritionRepository extends JpaRepository<MessageNutrition, Long> {

    // Récupérer les messages d'un patient par ordre chronologique
    List<MessageNutrition> findByPatientIdOrderByCreatedAtAsc(Long patientId);

    // Récupérer les messages entre patient et destinataire
    List<MessageNutrition> findByPatientIdAndReceiverId(Long patientId, Long receiverId);

    // ✅ CORRIGÉ : Récupérer les messages où le nutritionniste est sender OU receiver
    @Query("SELECT m FROM MessageNutrition m WHERE m.senderId = :userId OR m.receiverId = :userId ORDER BY m.createdAt ASC")
    List<MessageNutrition> findMessagesByUserId(@Param("userId") Long userId);

    // Récupérer la conversation entre nutritionniste et patient
    @Query("SELECT m FROM MessageNutrition m WHERE (m.senderId = :nutritionistId OR m.receiverId = :nutritionistId) AND m.patientId = :patientId ORDER BY m.createdAt ASC")
    List<MessageNutrition> findConversationBetween(@Param("nutritionistId") Long nutritionistId,
                                                   @Param("patientId") Long patientId);

    // Compter les messages non lus pour un destinataire
    long countByReceiverIdAndIsReadFalse(Long receiverId);

    // Marquer tous les messages comme lus
    @Modifying
    @Query("UPDATE MessageNutrition m SET m.isRead = true WHERE m.patientId = :patientId AND m.receiverId = :receiverId")
    void markAllAsRead(@Param("patientId") Long patientId, @Param("receiverId") Long receiverId);
}