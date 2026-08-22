package com.pravesh.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

// One row per status transition, including the initial ACTIVE raise.
@Entity
@Table(name = "sos_status_history")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class SosStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sos_alert_id", nullable = false)
    private SosAlert sosAlert;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SosStatus status;

    // Set server-side from the authenticated caller, never from the request body.
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by_user_id", nullable = false)
    private User changedBy;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @PrePersist
    protected void onCreate() {
        this.changedAt = LocalDateTime.now();
    }
}
