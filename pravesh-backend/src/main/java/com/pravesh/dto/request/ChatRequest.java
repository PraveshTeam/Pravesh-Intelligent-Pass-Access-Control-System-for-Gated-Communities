package com.pravesh.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ChatRequest(
        @NotBlank(message = "Message is required")
        String message,

        // Optional short history for follow-up context, oldest first.
        java.util.List<ChatTurn> history
) {
    public record ChatTurn(String role, String text) {}  // role: "user" | "assistant"
}