package com.pravesh.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePostRequest(

        @NotBlank(message = "Category is required")
        @Size(max = 30)
        String category,

        @NotBlank(message = "Title is required")
        @Size(max = 150, message = "Title must be under 150 characters")
        String title,

        @NotBlank(message = "Body is required")
        String body

) {}
