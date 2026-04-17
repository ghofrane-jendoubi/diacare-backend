package tn.esprit.spring.diacarebackend.repository;

import org.springframework.stereotype.Repository;
import tn.esprit.spring.diacarebackend.entities.ChatbotConversation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
@Repository
public interface ChatbotConversationRepository extends JpaRepository<ChatbotConversation, Long> {
    Optional<ChatbotConversation> findBySessionId(String sessionId);
}
