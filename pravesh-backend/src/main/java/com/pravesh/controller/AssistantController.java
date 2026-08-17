package com.pravesh.controller;

import com.pravesh.dto.request.ChatRequest;
import com.pravesh.dto.response.ApiResponse;
import com.pravesh.dto.response.ChatResponse;
import com.pravesh.security.AuthenticatedUser;
import com.pravesh.service.GeminiClient;
import com.pravesh.service.PromptBuilder;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assistant")
@RequiredArgsConstructor
public class AssistantController {

    private static final int MAX_MESSAGE_LENGTH = 800;
    private static final int MAX_HISTORY_TURNS = 8;

    private final PromptBuilder promptBuilder;
    private final GeminiClient geminiClient;

    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<ChatResponse>> chat(
            @AuthenticationPrincipal AuthenticatedUser caller,
            @Valid @RequestBody ChatRequest req) {

        String message = req.message().trim();
        if (message.length() > MAX_MESSAGE_LENGTH) {
            message = message.substring(0, MAX_MESSAGE_LENGTH);
        }

        List<Map<String, String>> history = req.history() == null
                ? List.of()
                : req.history().stream()
                    .skip(Math.max(0, req.history().size() - MAX_HISTORY_TURNS))
                    .map(t -> Map.of("role", t.role(), "text", t.text()))
                    .toList();

        String systemPrompt = promptBuilder.buildSystemPrompt(caller.role());
        String reply = geminiClient.generateReply(systemPrompt, history, message);

        return ResponseEntity.ok(ApiResponse.ok("Reply generated", new ChatResponse(reply)));
    }
}