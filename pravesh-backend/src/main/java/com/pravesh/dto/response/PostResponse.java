package com.pravesh.dto.response;

import java.time.LocalDateTime;

public record PostResponse(
        Long id,
        Long authorId,
        String authorName,   // resolved via Feign to user-service; falls back to null gracefully
        String category,
        String title,
        String body,
        boolean pinned,
        int commentCount,
        LocalDateTime createdAt
) {}
