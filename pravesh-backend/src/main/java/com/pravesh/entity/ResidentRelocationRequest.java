package com.pravesh.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

import com.pravesh.entity.enums.RequestStatus;

@Entity
@Table(name = "resident_relocation_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResidentRelocationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Set server-side from the authenticated resident, never from the request body.
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resident_user_id", nullable = false)
    private Resident resident;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "old_flat_id", nullable = false)
    private Flat oldFlat;

    // Set server-side from the resident's current JWT societyId claim.
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "old_society_id", nullable = false)
    private Society oldSociety;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_society_id", nullable = false)
    private Society targetSociety;

    @Column(name = "claimed_flat_number", nullable = false, length = 20)
    private String claimedFlatNumber;

    @Column(name = "tower", length = 20)
    private String tower;

    @Column(name = "document_type", nullable = false, length = 30)
    private String documentType;

    @Column(name = "document_path", nullable = false, length = 255)
    private String documentPath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status;

    @Column(name = "admin_notes", length = 255)
    private String adminNotes;

    // Set server-side from the reviewing admin.
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewer;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.status = RequestStatus.PENDING;
    }
}
