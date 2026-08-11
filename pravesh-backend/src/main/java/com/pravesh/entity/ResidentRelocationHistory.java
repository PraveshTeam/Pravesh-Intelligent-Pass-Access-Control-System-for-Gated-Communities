package com.pravesh.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "resident_relocation_history")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ResidentRelocationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "resident_user_id", nullable = false)
    private Long residentUserId;

    @Column(name = "old_flat_id", nullable = false)
    private Long oldFlatId;

    @Column(name = "old_society_id", nullable = false)
    private Long oldSocietyId;

    @Column(name = "new_flat_id", nullable = false)
    private Long newFlatId;

    @Column(name = "new_society_id", nullable = false)
    private Long newSocietyId;

    @Column(name = "approved_by", nullable = false)
    private Long approvedBy;

    @Column(name = "relocated_at", nullable = false)
    private LocalDateTime relocatedAt;

    @PrePersist
    protected void onCreate() {
        this.relocatedAt = LocalDateTime.now();
    }
}