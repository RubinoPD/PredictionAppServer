package eif.viko.lt.predictionappserver.Controllers;

import eif.viko.lt.predictionappserver.Entities.ChatHistory;
import eif.viko.lt.predictionappserver.Entities.ChatUser;
import eif.viko.lt.predictionappserver.Entities.Role;
import eif.viko.lt.predictionappserver.Repositories.ChatHistoryRepository;
import eif.viko.lt.predictionappserver.Services.ChatBotService;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chatbot")
public class ChatBotController {

    private final ChatBotService chatBotService;
    private final ChatHistoryRepository chatHistoryRepository;

    public ChatBotController(ChatBotService chatBotService, ChatHistoryRepository chatHistoryRepository) {
        this.chatBotService = chatBotService;
        this.chatHistoryRepository = chatHistoryRepository;
    }

    @GetMapping("/ask")
    public ResponseEntity<Map<String, Object>> ask(@RequestParam String question) {
        ChatUser currentUser = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof ChatUser) {
            currentUser = (ChatUser) authentication.getPrincipal();
        }

        Map<String, Object> response = chatBotService.processQuestion(question, currentUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<List<ChatHistory>> getChatHistory() {
        // Tik autentifikuotiems vartotojams
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            List<ChatHistory> history = chatHistoryRepository.findAll();
            return ResponseEntity.ok(history);
        }
        return ResponseEntity.status(401).build();
    }

    @PostMapping("/admin/response")
    public ResponseEntity<?> addCategoryResponse(@RequestBody CategoryResponseDto dto) {
        // Tik administratoriams ir mokytojams
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof ChatUser) {
            ChatUser currentUser = (ChatUser) authentication.getPrincipal();
            if (currentUser.getRole() == Role.ADMIN ||
                    currentUser.getRole() == Role.TEACHER) {

                chatBotService.addCategoryResponse(dto.getCategory(), dto.getResponse());
                return ResponseEntity.ok().build();
            }
        }
        return ResponseEntity.status(403).build();
    }

    @GetMapping("/admin/responses")
    public ResponseEntity<Map<String, String>> getAllResponses() {
        // Tik administratoriams ir mokytojams
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof ChatUser) {
            ChatUser currentUser = (ChatUser) authentication.getPrincipal();
            if (currentUser.getRole() == Role.ADMIN ||
                    currentUser.getRole() == Role.TEACHER) {

                Map<String, String> responses = chatBotService.getAllCategoryResponses();
                return ResponseEntity.ok(responses);
            }
        }
        return ResponseEntity.status(403).build();
    }

    @Data
    public static class CategoryResponseDto {
        private String category;
        private String response;
    }
}