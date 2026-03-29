package tn.esprit.spring.diacarebackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.spring.diacarebackend.dto.ConversationDTO;
import tn.esprit.spring.diacarebackend.entities.Message;
import tn.esprit.spring.diacarebackend.entities.User;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("SELECT m FROM Message m WHERE (m.sender = :user1 AND m.receiver = :user2) OR (m.sender = :user2 AND m.receiver = :user1) ORDER BY m.sentAt ASC")
    List<Message> findConversation(@Param("user1") User user1, @Param("user2") User user2);

    @Modifying
    @Transactional
    @Query("UPDATE Message m SET m.seen = true WHERE m.receiver = :receiver AND m.seen = false")
    void markAllAsRead(@Param("receiver") User receiver);

    @Modifying
    @Transactional
    @Query("UPDATE Message m SET m.seen = true WHERE m.receiver = :doctor AND m.sender = :patient AND m.seen = false")
    void markMessagesAsReadBetween(@Param("doctor") User doctor, @Param("patient") User patient);

    //  Requête native pour les conversations d'un patient
    @Query(value = "SELECT " +
            "u.id, " +
            "CONCAT(u.first_name, ' ', u.last_name), " +
            "u.profile_picture, " +
            "p.diabetes_type, " +
            "(SELECT m.content FROM messages m WHERE (m.sender_id = u.id AND m.receiver_id = :patientId) OR (m.sender_id = :patientId AND m.receiver_id = u.id) ORDER BY m.sent_at DESC LIMIT 1), " +
            "(SELECT m.sent_at FROM messages m WHERE (m.sender_id = u.id AND m.receiver_id = :patientId) OR (m.sender_id = :patientId AND m.receiver_id = u.id) ORDER BY m.sent_at DESC LIMIT 1), " +
            "(SELECT m.sender_id FROM messages m WHERE (m.sender_id = u.id AND m.receiver_id = :patientId) OR (m.sender_id = :patientId AND m.receiver_id = u.id) ORDER BY m.sent_at DESC LIMIT 1), " +
            "(SELECT COUNT(*) FROM messages m WHERE m.sender_id = u.id AND m.receiver_id = :patientId AND m.seen = false), " +
            "false " +
            "FROM users u " +
            "INNER JOIN patients p ON u.id = p.id " +
            "WHERE u.id IN (" +
            "SELECT DISTINCT m.sender_id FROM messages m WHERE m.receiver_id = :patientId AND m.sender_id != :patientId " +
            "UNION " +
            "SELECT DISTINCT m.receiver_id FROM messages m WHERE m.sender_id = :patientId AND m.receiver_id != :patientId" +
            ")", nativeQuery = true)
    List<Object[]> findConversationsByPatientNative(@Param("patientId") Long patientId);

    // ✅ Requête native pour les conversations d'un médecin
    @Query(value = """
    SELECT
        u.id,
        CONCAT(u.first_name, ' ', u.last_name),
        p.profile_picture,
        NULL,
        (SELECT m2.content FROM messages m2
         WHERE (m2.sender_id = u.id AND m2.receiver_id = :doctorId)
            OR (m2.sender_id = :doctorId AND m2.receiver_id = u.id)
         ORDER BY m2.sent_at DESC LIMIT 1),
        (SELECT m2.sent_at FROM messages m2
         WHERE (m2.sender_id = u.id AND m2.receiver_id = :doctorId)
            OR (m2.sender_id = :doctorId AND m2.receiver_id = u.id)
         ORDER BY m2.sent_at DESC LIMIT 1),
        (SELECT m2.sender_id FROM messages m2
         WHERE (m2.sender_id = u.id AND m2.receiver_id = :doctorId)
            OR (m2.sender_id = :doctorId AND m2.receiver_id = u.id)
         ORDER BY m2.sent_at DESC LIMIT 1),
        (SELECT COUNT(*) FROM messages m2
         WHERE m2.sender_id = u.id
           AND m2.receiver_id = :doctorId
           AND m2.seen = false)
    FROM users u
    INNER JOIN patients p ON u.id = p.id
    WHERE u.id IN (
        SELECT DISTINCT m.sender_id FROM messages m
        WHERE m.receiver_id = :doctorId AND m.sender_id != :doctorId
        UNION
        SELECT DISTINCT m.receiver_id FROM messages m
        WHERE m.sender_id = :doctorId AND m.receiver_id != :doctorId
    )
    """, nativeQuery = true)
    List<Object[]> findConversationsByDoctorNative(@Param("doctorId") Long doctorId);
    @Query(value = """
    SELECT
        u.id,
        CONCAT(u.first_name, ' ', u.last_name) AS doctorName,
        doc.profile_picture,
        doc.speciality,
        (SELECT m2.content FROM messages m2
         WHERE (m2.sender_id = u.id AND m2.receiver_id = :patientId)
            OR (m2.sender_id = :patientId AND m2.receiver_id = u.id)
         ORDER BY m2.sent_at DESC LIMIT 1) AS lastMessage,
        (SELECT m2.sent_at FROM messages m2
         WHERE (m2.sender_id = u.id AND m2.receiver_id = :patientId)
            OR (m2.sender_id = :patientId AND m2.receiver_id = u.id)
         ORDER BY m2.sent_at DESC LIMIT 1) AS lastMessageTime,
        (SELECT m2.sender_id FROM messages m2
         WHERE (m2.sender_id = u.id AND m2.receiver_id = :patientId)
            OR (m2.sender_id = :patientId AND m2.receiver_id = u.id)
         ORDER BY m2.sent_at DESC LIMIT 1) AS lastSender,
        (SELECT COUNT(*) FROM messages m2
         WHERE m2.sender_id = u.id AND m2.receiver_id = :patientId AND m2.seen = false) AS unreadCount
    FROM users u
    JOIN doctors doc ON u.id = doc.id
    WHERE u.id IN (
        SELECT DISTINCT sender_id FROM messages WHERE receiver_id = :patientId AND sender_id != :patientId
        UNION
        SELECT DISTINCT receiver_id FROM messages WHERE sender_id = :patientId AND receiver_id != :patientId
    )
    ORDER BY lastMessageTime DESC
    """, nativeQuery = true)
    List<Object[]> findDoctorConversationsByPatientNative(@Param("patientId") Long patientId);}