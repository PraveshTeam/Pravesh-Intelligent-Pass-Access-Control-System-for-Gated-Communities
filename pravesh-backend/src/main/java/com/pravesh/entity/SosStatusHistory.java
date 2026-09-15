package com.pravesh.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

// One row per status transition (including the initial ACTIVE raise) --
// this is the real audit trail SosAlert.acknowledgedBy alone can't provide,
// since that single field only ever captured ONE step (who acknowledged),
// with no record of who set HELP_ON_THE_WAY or who ultimately RESOLVED it.
@Entity
@Table(name = "sos_status_history")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class SosStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sos_alert_id", nullable = false)
    private Long sosAlertId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SosStatus status;

    @Column(name = "changed_by_user_id", nullable = false)
    private Long changedByUserId;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @PrePersist
    protected void onCreate() {
        this.changedAt = LocalDateTime.now();
    }
}
