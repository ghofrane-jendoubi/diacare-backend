package tn.esprit.spring.diacarebackend.repository;

import tn.esprit.spring.diacarebackend.entities.PrivateMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PrivateMessageRepository extends JpaRepository<PrivateMessage, Long> {
    List<PrivateMessage> findByReceiverIdOrderByCreatedAtDesc(Long receiverId);
    List<PrivateMessage> findBySenderIdOrderByCreatedAtDesc(Long senderId);
    List<PrivateMessage> findBySenderIdOrReceiverIdOrderByCreatedAtDesc(Long senderId, Long receiverId);
    long countByReceiverIdAndIsReadFalse(Long receiverId);
}