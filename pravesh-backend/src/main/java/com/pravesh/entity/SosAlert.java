package com.pravesh.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sos_alerts")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class SosAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Written server-side from the authenticated resident's own id, never
    // from the request body.
    @Column(name = "resident_user_id", nullable = false)
    private Long residentUserId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resident_user_id", insertable = false, updatable = false)
    private Resident resident;

    @Column(name = "flat_id", nullable = false)
    private Long flatId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flat_id", insertable = false, updatable = false)
    private Flat flat;

    // CRITICAL for multi-tenancy: set server-side from the resident's own JWT
    // societyId claim, never trusted from the client -- guards/admins fetch
    // "active alerts" scoped by this. Relationship is read-only for the same
    // reason as elsewhere in this codebase.
    @Column(name = "society_id", nullable = false)
    private Long societyId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "society_id", insertable = false, updatable = false)
    private Society society;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SosCategory category;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SosStatus status;

    // Written server-side from the acknowledging guard/admin's own id.
    @Column(name = "acknowledged_by")
    private Long acknowledgedBy;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acknowledged_by", insertable = false, updatable = false)
    private User acknowledgedByUser;

    @Column(name = "acknowledged_at")
    private LocalDateTime acknowledgedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Inverse of SosStatusHistory.sosAlertId -- full audit trail of every
    // status transition for this alert. Plain read-only mappedBy, no
    // cascade/orphanRemoval, matching the rest of this codebase (nothing
    // deletes a SosAlert today).
    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "sosAlert", fetch = FetchType.LAZY)
    private List<SosStatusHistory> statusHistory = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.status = SosStatus.ACTIVE;
    }
}
