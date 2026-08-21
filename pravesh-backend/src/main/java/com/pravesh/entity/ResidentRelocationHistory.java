package com.pravesh.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resident_user_id", insertable = false, updatable = false)
    private Resident resident;

    @Column(name = "old_flat_id", nullable = false)
    private Long oldFlatId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "old_flat_id", insertable = false, updatable = false)
    private Flat oldFlat;

    @Column(name = "old_society_id", nullable = false)
    private Long oldSocietyId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "old_society_id", insertable = false, updatable = false)
    private Society oldSociety;

    @Column(name = "new_flat_id", nullable = false)
    private Long newFlatId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "new_flat_id", insertable = false, updatable = false)
    private Flat newFlat;

    @Column(name = "new_society_id", nullable = false)
    private Long newSocietyId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "new_society_id", insertable = false, updatable = false)
    private Society newSociety;

    // Written server-side from the approving admin's own id.
    @Column(name = "approved_by", nullable = false)
    private Long approvedBy;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by", insertable = false, updatable = false)
    private User approver;

    @Column(name = "relocated_at", nullable = false)
    private LocalDateTime relocatedAt;

    @PrePersist
    protected void onCreate() {
        this.relocatedAt = LocalDateTime.now();
    }
}
