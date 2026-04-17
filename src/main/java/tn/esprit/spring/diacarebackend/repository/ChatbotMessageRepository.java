package tn.esprit.spring.diacarebackend.repository;

import org.springframework.stereotype.Repository;
import tn.esprit.spring.diacarebackend.entities.ChatbotMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
@Repository
public interface ChatbotMessageRepository extends JpaRepository<ChatbotMessage, Long> {
    List<ChatbotMessage> findByConversationIdOrderByCreatedAtAsc(Long conversationId);
}
