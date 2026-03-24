package tn.esprit.spring.diacarebackend.repository;

import tn.esprit.spring.diacarebackend.entities.ChatbotConversation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ChatbotConversationRepository extends JpaRepository<ChatbotConversation, Long> {
    Optional<ChatbotConversation> findBySessionId(String sessionId);
}