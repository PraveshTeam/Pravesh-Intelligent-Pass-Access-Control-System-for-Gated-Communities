package com.pravesh.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pravesh.entity.enums.RequestStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "society_registration_requests")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class SocietyRegistrationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Written server-side from the authenticated caller's own id, never
    // from the request body.
    @Column(name = "admin_user_id", nullable = false)
    private Long adminUserId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_user_id", insertable = false, updatable = false)
    private User adminUser;

    @Column(name = "society_name", nullable = false, length = 150)
    private String societyName;

    @Column(length = 255)
    private String address;

    @Column(length = 50)
    private String city;

    @Column(name = "document_path", nullable = false, length = 255)
    private String documentPath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private RequestStatus status = RequestStatus.PENDING;

    @Column(name = "admin_notes", columnDefinition = "TEXT")
    private String adminNotes;

    // Written server-side from the reviewing (platform) admin's own id.
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
