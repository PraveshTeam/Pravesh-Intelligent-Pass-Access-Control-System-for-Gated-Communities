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

    // Multi-tenancy key: set from the scanning guard's JWT societyId claim.
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "society_id", nullable = false)
    private Society society;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gate_id", nullable = false)
    private Gate gate;

    // Set server-side from the authenticated guard's principal.
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guard_user_id", nullable = false)
    private Guard guard;

    @Column(name = "visitor_name", nullable = false, length = 100)
    private String visitorName;

    @Column(name = "visitor_phone", length = 15)
    private String visitorPhone;

    @Column(name = "claimed_flat_number", nullable = false, length = 20)
    private String claimedFlatNumber;

    @Column(name = "reason", length = 255)
    private String reason;

    // Resolved server-side from claimedFlatNumber + society, not client-supplied.
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flat_id")
    private Flat flat;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resident_id")
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
