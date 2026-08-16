package com.pravesh.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "gate_entry_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GateEntryRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // CRITICAL for multi-tenancy: set server-side from the scanning guard's
    // own JWT societyId claim, never trusted from the request -- relationship
    // kept read-only for the same reason.
    @Column(name = "society_id", nullable = false)
    private Long societyId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "society_id", insertable = false, updatable = false)
    private Society society;

    @Column(name = "gate_id", nullable = false)
    private Long gateId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gate_id", insertable = false, updatable = false)
    private Gate gate;

    // Written server-side from the authenticated guard's own principal,
    // never from the request body.
    @Column(name = "guard_user_id", nullable = false)
    private Long guardUserId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guard_user_id", insertable = false, updatable = false)
    private Guard guard;

    @Column(name = "visitor_name", nullable = false, length = 100)
    private String visitorName;

    @Column(name = "visitor_phone", length = 15)
    private String visitorPhone;

    @Column(name = "claimed_flat_number", nullable = false, length = 20)
    private String claimedFlatNumber;

    @Column(name = "reason", length = 255)
    private String reason;

    // Resolved server-side (via claimedFlatNumber + societyId lookup), not
    // client-supplied -- relationship read-only for the same reason.
    @Column(name = "flat_id")
    private Long flatId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flat_id", insertable = false, updatable = false)
    private Flat flat;

    @Column(name = "resident_id")
    private Long residentId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resident_id", insertable = false, updatable = false)
    private Resident resident;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GateRequestStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.status = GateRequestStatus.PENDING;
        this.expiresAt = this.createdAt.plusMinutes(5);
    }
}
