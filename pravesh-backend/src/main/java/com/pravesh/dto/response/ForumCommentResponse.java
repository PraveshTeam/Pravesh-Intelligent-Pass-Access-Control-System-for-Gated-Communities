package com.pravesh.dto.response;

import java.time.LocalDateTime;

public record ForumCommentResponse(
        Long id,
        Long authorId,
        String authorName,
        String body,
        LocalDateTime createdAt
) {}
