package com.pravesh.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

// This is deliberately a plain Trip entity, not the roadmap's generalized
// GroupActivity-with-a-type-discriminator design -- scope was trimmed to
// Trip Buddy only (no Classes/Activities/Interest Groups, no is_official,
// no convert-to-official admin step). See project memory for the scope decision.
@Entity
@Table(name = "trips")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "creator_id", nullable = false)
    private Long creatorId;

    // Society scoping built in from the START this time -- learned from the
    // payment-service / forum-service cross-tenant leaks found and fixed earlier.
    @Column(name = "society_id", nullable = false)
    private Long societyId;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private int capacity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TripStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.status = TripStatus.OPEN;
    }
}
