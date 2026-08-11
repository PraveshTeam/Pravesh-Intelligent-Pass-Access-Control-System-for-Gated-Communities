package com.pravesh.entity;

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

    @Column(name = "society_id", nullable = false)
    private Long societyId;

    @Column(name = "gate_id", nullable = false)
    private Long gateId;

    @Column(name = "guard_user_id", nullable = false)
    private Long guardUserId;

    @Column(name = "visitor_name", nullable = false, length = 100)
    private String visitorName;

    @Column(name = "visitor_phone", length = 15)
    private String visitorPhone;

    @Column(name = "claimed_flat_number", nullable = false, length = 20)
    private String claimedFlatNumber;

    @Column(name = "reason", length = 255)
    private String reason;

    @Column(name = "flat_id")
    private Long flatId;

    @Column(name = "resident_id")
    private Long residentId;

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