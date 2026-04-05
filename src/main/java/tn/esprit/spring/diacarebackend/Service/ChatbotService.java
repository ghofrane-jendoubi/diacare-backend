package tn.esprit.spring.diacarebackend.Service;

import tn.esprit.spring.diacarebackend.entities.ChatbotConversation;
import tn.esprit.spring.diacarebackend.entities.ChatbotMessage;
import tn.esprit.spring.diacarebackend.repository.ChatbotConversationRepository;
import tn.esprit.spring.diacarebackend.repository.ChatbotMessageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class ChatbotService {

    private final ChatbotConversationRepository convRepo;
    private final ChatbotMessageRepository msgRepo;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("$AIzaSyCaqVu7MY4T3h-bfK7xA1riKpexCR-MlJU")
    private String geminiKey;

    // Prompt système pour le chatbot diabète
    private static final String SYSTEM_PROMPT =
            "Tu es DiaCare Assistant, un assistant médical spécialisé en diabétologie. " +
                    "Tu réponds UNIQUEMENT en français. " +
                    "Tu es bienveillant, précis et professionnel. " +
                    "Tu réponds uniquement aux questions liées au diabète, à la glycémie, " +
                    "à l'alimentation pour diabétiques, aux médicaments anti-diabétiques, " +
                    "à l'activité physique et au mode de vie. " +
                    "Si la question n'est pas liée au diabète ou à la santé, " +
                    "réponds poliment que tu es spécialisé en diabétologie uniquement. " +
                    "Ne donne JAMAIS de diagnostic médical définitif. " +
                    "Utilise des emojis pour rendre tes réponses plus claires. " +
                    "Limite tes réponses à 200 mots maximum. " +
                    "Format tes réponses avec des points et des sauts de ligne.";

    public ChatbotService(ChatbotConversationRepository convRepo,
                          ChatbotMessageRepository msgRepo) {
        this.convRepo = convRepo;
        this.msgRepo = msgRepo;
    }

    @Transactional
    public Map<String, Object> chat(String sessionId, String userMessage, Long patientId) {

        // Récupérer ou créer la conversation
        ChatbotConversation conv = convRepo.findBySessionId(sessionId)
                .orElseGet(() -> {
                    ChatbotConversation c = new ChatbotConversation();
                    c.setSessionId(sessionId);
                    c.setPatientId(patientId);
                    return convRepo.save(c);
                });

        // Sauvegarder message patient
        ChatbotMessage patientMsg = new ChatbotMessage();
        patientMsg.setConversationId(conv.getId());
        patientMsg.setSender(ChatbotMessage.Sender.PATIENT);
        patientMsg.setMessage(userMessage);
        msgRepo.save(patientMsg);

        // Récupérer l'historique pour le contexte
        List<ChatbotMessage> history =
                msgRepo.findByConversationIdOrderByCreatedAtAsc(conv.getId());

        // Générer réponse IA
        String botResponse = generateAIResponse(userMessage, history);

        // Sauvegarder réponse bot
        ChatbotMessage botMsg = new ChatbotMessage();
        botMsg.setConversationId(conv.getId());
        botMsg.setSender(ChatbotMessage.Sender.BOT);
        botMsg.setMessage(botResponse);
        msgRepo.save(botMsg);

        return Map.of(
                "sessionId", sessionId,
                "response", botResponse,
                "conversationId", conv.getId()
        );
    }

    public List<ChatbotMessage> getHistory(String sessionId) {
        return convRepo.findBySessionId(sessionId)
                .map(c -> msgRepo.findByConversationIdOrderByCreatedAtAsc(c.getId()))
                .orElse(new ArrayList<>());
    }

    private String generateAIResponse(String userMessage,
                                      List<ChatbotMessage> history) {
        try {
            // Essayer Gemini d'abord
            return callGemini(userMessage, history);
        } catch (Exception e) {
            System.err.println("Gemini échoué, fallback rule-based: " + e.getMessage());
            // Fallback sur les règles si Gemini échoue
            return generateRuleBasedResponse(userMessage.toLowerCase().trim());
        }
    }

    private String callGemini(String userMessage,
                              List<ChatbotMessage> history) {
        String url = "https://generativelanguage.googleapis.com/v1beta/" +
                "models/gemini-1.5-flash:generateContent?key=" + geminiKey;

        // Construire le contexte avec l'historique (max 10 derniers messages)
        List<Map<String, Object>> contents = new ArrayList<>();

        // Ajouter l'historique récent (max 8 messages pour ne pas dépasser les tokens)
        int startIdx = Math.max(0, history.size() - 8);
        for (int i = startIdx; i < history.size() - 1; i++) {
            ChatbotMessage msg = history.get(i);
            Map<String, Object> content = new HashMap<>();
            content.put("role", msg.getSender() == ChatbotMessage.Sender.PATIENT
                    ? "user" : "model");
            List<Map<String, Object>> parts = new ArrayList<>();
            Map<String, Object> part = new HashMap<>();
            part.put("text", msg.getMessage());
            parts.add(part);
            content.put("parts", parts);
            contents.add(content);
        }

        // Ajouter le message actuel avec le prompt système
        Map<String, Object> currentContent = new HashMap<>();
        currentContent.put("role", "user");
        List<Map<String, Object>> currentParts = new ArrayList<>();
        Map<String, Object> currentPart = new HashMap<>();
        currentPart.put("text", SYSTEM_PROMPT + "\n\nQuestion du patient: " + userMessage);
        currentParts.add(currentPart);
        currentContent.put("parts", currentParts);
        contents.add(currentContent);

        // Configuration de génération
        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", 0.7);
        generationConfig.put("maxOutputTokens", 400);
        generationConfig.put("topP", 0.8);

        Map<String, Object> request = new HashMap<>();
        request.put("contents", contents);
        request.put("generationConfig", generationConfig);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                url, new HttpEntity<>(request, headers), Map.class);

        if (response.getBody() != null) {
            List<Map> candidates = (List<Map>) response.getBody().get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                Map content = (Map) candidates.get(0).get("content");
                List<Map> parts = (List<Map>) content.get("parts");
                if (parts != null && !parts.isEmpty()) {
                    return (String) parts.get(0).get("text");
                }
            }
        }
        throw new RuntimeException("Réponse Gemini vide");
    }

    // ===== FALLBACK RULE-BASED (si Gemini échoue) =====
    private String generateRuleBasedResponse(String message) {

        if (containsAny(message, "bonjour", "bonsoir", "salut", "hello")) {
            return "👋 Bonjour ! Je suis DiaCare Assistant.\n\n" +
                    "Je suis là pour vous aider avec vos questions sur le diabète.\n\n" +
                    "Comment puis-je vous aider aujourd'hui ?";
        }

        if (containsAny(message, "glycémie", "glycemie", "sucre", "glucose")) {
            if (containsAny(message, "haute", "élevé", "hyper")) {
                return "⚠️ **Hyperglycémie**\n\n" +
                        "• Buvez de l'eau\n" +
                        "• Marchez 15-20 minutes\n" +
                        "• Prenez votre traitement\n\n" +
                        "🚨 Si > 3 g/L avec malaise : appelez le 15";
            }
            if (containsAny(message, "basse", "faible", "hypo")) {
                return "🚨 **Hypoglycémie**\n\n" +
                        "• 3 sucres OU 1 verre de jus de fruit\n" +
                        "• Attendez 15 minutes\n" +
                        "• Remesurer la glycémie\n\n" +
                        "⚠️ Toujours avoir du sucre sur soi !";
            }
            return "📊 **Valeurs normales :**\n" +
                    "• À jeun : 0,70 – 1,00 g/L\n" +
                    "• Après repas : < 1,40 g/L\n" +
                    "• HbA1c cible : < 7%";
        }

        if (containsAny(message, "manger", "alimentation", "régime", "repas")) {
            return "🥗 **Alimentation diabète :**\n\n" +
                    "✅ À privilégier : légumes, céréales complètes, protéines maigres\n" +
                    "❌ À limiter : sucres rapides, sodas, pain blanc\n\n" +
                    "💡 Commencez toujours par les légumes !";
        }

        if (containsAny(message, "sport", "exercice", "marche", "activité")) {
            return "🏃 **Activité physique :**\n\n" +
                    "• 30 min de marche, 5 fois/semaine\n" +
                    "• Mesurez la glycémie avant\n" +
                    "• Ayez du sucre sur vous\n\n" +
                    "✅ L'exercice améliore la sensibilité à l'insuline !";
        }

        if (containsAny(message, "urgence", "malaise", "samu", "danger")) {
            return "🚨 **Urgences :**\n\n" +
                    "📞 SAMU : 15\n" +
                    "📞 Pompiers : 18\n" +
                    "📞 Urgences EU : 112\n\n" +
                    "**Appeler si :** perte de connaissance, glycémie > 3 g/L";
        }

        return "🤖 Je suis DiaCare Assistant, spécialisé en diabétologie.\n\n" +
                "Posez-moi des questions sur :\n" +
                "• 🩸 La glycémie\n• 🥗 L'alimentation\n" +
                "• 💊 Les médicaments\n• 🏃 Le sport\n• 🚨 Les urgences";
    }

    private boolean containsAny(String message, String... keywords) {
        for (String keyword : keywords) {
            if (message.contains(keyword)) return true;
        }
        return false;
    }
}