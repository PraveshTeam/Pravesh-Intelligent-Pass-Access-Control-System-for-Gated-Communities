package com.pravesh.entity;

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

    @Column(name = "resident_user_id", nullable = false)
    private Long residentUserId;

    @Column(name = "old_flat_id", nullable = false)
    private Long oldFlatId;

    @Column(name = "old_society_id", nullable = false)
    private Long oldSocietyId;

    @Column(name = "target_society_id", nullable = false)
    private Long targetSocietyId;

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

    @Column(name = "reviewed_by")
    private Long reviewedBy;

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