package tn.esprit.spring.diacarebackend.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.spring.diacarebackend.dto.ConversationDTO;
import tn.esprit.spring.diacarebackend.entities.Message;
import tn.esprit.spring.diacarebackend.entities.Patient;
import tn.esprit.spring.diacarebackend.entities.User;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("SELECT m FROM Message m WHERE " +
            "(m.sender = :user1 AND m.receiver = :user2) OR " +
            "(m.sender = :user2 AND m.receiver = :user1) " +
            "ORDER BY m.sentAt ASC")
    List<Message> findConversation(@Param("user1") User user1, @Param("user2") User user2);

    List<Message> findByReceiverAndSeenFalseOrderBySentAtDesc(User receiver);

    @Modifying
    @Transactional
    @Query("UPDATE Message m SET m.seen = true, m.seenAt = CURRENT_TIMESTAMP() " +
            "WHERE m.receiver = :receiver AND m.seen = false")
    int markAllAsRead(@Param("receiver") User receiver);

    // ✅ Nouvelle méthode pour récupérer les conversations d'un patient
    @Query("SELECT NEW tn.esprit.spring.diacarebackend.dto.ConversationDTO(" +
            "d.id, d, " +
            "(SELECT m.content FROM Message m WHERE " +
            " (m.sender.id = d.id AND m.receiver.id = :patientId) OR " +
            " (m.sender.id = :patientId AND m.receiver.id = d.id) " +
            "ORDER BY m.sentAt DESC LIMIT 1), " +
            "(SELECT MAX(m.sentAt) FROM Message m WHERE " +
            " (m.sender.id = d.id AND m.receiver.id = :patientId) OR " +
            " (m.sender.id = :patientId AND m.receiver.id = d.id)), " +
            "(SELECT COUNT(m) FROM Message m WHERE " +
            " m.sender.id = d.id AND m.receiver.id = :patientId AND m.seen = false)) " +
            "FROM Doctor d " +
            "WHERE EXISTS (SELECT m FROM Message m WHERE " +
            " (m.sender.id = d.id AND m.receiver.id = :patientId) OR " +
            " (m.sender.id = :patientId AND m.receiver.id = d.id))")
    List<ConversationDTO> findConversationsByPatient(@Param("patientId") Long patientId);
}