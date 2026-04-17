package tn.esprit.spring.diacarebackend.controller;

import tn.esprit.spring.diacarebackend.entities.ChatbotMessage;
import tn.esprit.spring.diacarebackend.services.ChatbotService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chatbot")
@CrossOrigin(origins = "http://localhost:4200")
public class ChatbotController {

    private final ChatbotService chatbotService;

    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> chat(@RequestBody Map<String, Object> body) {
        String sessionId = (String) body.get("sessionId");
        String message = (String) body.get("message");
        Long patientId = body.get("patientId") != null ?
                Long.valueOf(body.get("patientId").toString()) : 1L;

        return ResponseEntity.ok(chatbotService.chat(sessionId, message, patientId));
    }

    @GetMapping("/history/{sessionId}")
    public ResponseEntity<List<ChatbotMessage>> getHistory(@PathVariable String sessionId) {
        return ResponseEntity.ok(chatbotService.getHistory(sessionId));
    }
}
