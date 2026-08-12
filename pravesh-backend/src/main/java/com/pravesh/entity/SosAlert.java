package com.pravesh.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sos_alerts")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class SosAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "resident_user_id", nullable = false)
    private Long residentUserId;

    @Column(name = "flat_id", nullable = false)
    private Long flatId;

    @Column(name = "society_id", nullable = false)
    private Long societyId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SosCategory category;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SosStatus status;

    @Column(name = "acknowledged_by")
    private Long acknowledgedBy;

    @Column(name = "acknowledged_at")
    private LocalDateTime acknowledgedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.status = SosStatus.ACTIVE;
    }
}