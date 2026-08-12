package com.pravesh.dto.response;

import java.time.LocalDateTime;

public record TripCommentResponse(
        Long id,
        Long authorId,
        String authorName,
        String body,
        LocalDateTime createdAt
) {}
