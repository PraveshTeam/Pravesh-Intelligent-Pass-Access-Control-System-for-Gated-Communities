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

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resident_user_id", nullable = false)
    private Resident resident;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "old_flat_id", nullable = false)
    private Flat oldFlat;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "old_society_id", nullable = false)
    private Society oldSociety;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "new_flat_id", nullable = false)
    private Flat newFlat;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "new_society_id", nullable = false)
    private Society newSociety;

    // Set server-side from the approving admin.
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by", nullable = false)
    private User approver;

    @Column(name = "relocated_at", nullable = false)
    private LocalDateTime relocatedAt;

    @PrePersist
    protected void onCreate() {
        this.relocatedAt = LocalDateTime.now();
    }
}
