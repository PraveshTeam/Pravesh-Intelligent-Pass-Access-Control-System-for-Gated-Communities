package com.pravesh.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", insertable = false, updatable = false)
    private Trip trip;

    // Written server-side from the authenticated caller's own id, never
    // from the request body -- kept read-only on the relationship too.
    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", insertable = false, updatable = false)
    private User author;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String body;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
