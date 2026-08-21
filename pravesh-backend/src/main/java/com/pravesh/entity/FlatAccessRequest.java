package com.pravesh.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pravesh.entity.enums.DocumentType;
import com.pravesh.entity.enums.RequestStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "flat_access_requests")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlatAccessRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Written server-side from the authenticated caller's own id, never
    // from the request body.
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    // CRITICAL for multi-tenancy: set server-side from JWT, never trusted
    // from the request body. Relationship read-only for the same reason.
    @Column(name = "society_id", nullable = false)
    private Long societyId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "society_id", insertable = false, updatable = false)
    private Society society;

    @Column(name = "claimed_flat_number", nullable = false, length = 20)
    private String claimedFlatNumber;

    @Column(length = 20)
    private String tower;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 20)
    private DocumentType documentType;

    @Column(name = "document_path", nullable = false, length = 255)
    private String documentPath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private RequestStatus status = RequestStatus.PENDING;

    @Column(name = "admin_notes", columnDefinition = "TEXT")
    private String adminNotes;

    // Written server-side from the reviewing admin's own id, never from the
    // request body.
    @Column(name = "reviewed_by")
    private Long reviewedBy;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by", insertable = false, updatable = false)
    private User reviewer;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
