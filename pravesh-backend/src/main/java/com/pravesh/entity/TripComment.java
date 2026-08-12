package com.pravesh.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

// Mirrors forum-service's comment shape (same design pattern the roadmap calls
// for reusing) but lives in its OWN table/database -- microservices don't
// share tables across service boundaries, so "reuse" here means the same
// entity design, not a literal shared row.
@Entity
@Table(name = "trip_comments")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class TripComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "trip_id", nullable = false)
    private Long tripId;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String body;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
