package com.pravesh.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", insertable = false, updatable = false)
    private User creator;

    // Society scoping built in from the START this time -- learned from the
    // payment-service / forum-service cross-tenant leaks found and fixed
    // earlier. Set server-side from the creator's own JWT societyId claim;
    // the relationship is read-only for the same reason as PaymentOrder.
    @Column(name = "society_id", nullable = false)
    private Long societyId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "society_id", insertable = false, updatable = false)
    private Society society;

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

    // Plain read-only mappedBy collections, same as every other relationship
    // in this codebase -- no cascade/orphanRemoval, since nothing here
    // deletes a Trip today and adding cascade would introduce new delete
    // behavior no service currently expects.
    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "trip", fetch = FetchType.LAZY)
    private List<TripComment> comments = new ArrayList<>();

    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "trip", fetch = FetchType.LAZY)
    private List<TripJoinRequest> joinRequests = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.status = TripStatus.OPEN;
    }
}
