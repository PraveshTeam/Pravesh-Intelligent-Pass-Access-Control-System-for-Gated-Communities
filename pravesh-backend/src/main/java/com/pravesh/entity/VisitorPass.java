package com.pravesh.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pravesh.entity.enums.PassStatus;
import com.pravesh.entity.enums.PassType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "visitor_passes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VisitorPass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Raw FK -- authoritative. Always set server-side from the authenticated
    // resident's own id (see PassService.createPass(residentId, ...)),
    // never from request body, so IDOR/impersonation isn't possible via
    // the relationship either.
    @Column(name = "resident_id", nullable = false)
    private Long residentId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resident_id", insertable = false, updatable = false)
    private Resident resident;

    @Column(nullable = false, unique = true, length = 36)
    private String uuid;

    @Column(name = "visitor_name", nullable = false, length = 100)
    private String visitorName;

    @Column(name = "visitor_phone", length = 15)
    private String visitorPhone;

    @Enumerated(EnumType.STRING)
    @Column(name = "pass_type", nullable = false, length = 20)
    private PassType passType;

    @Column(name = "uses_allowed")
    private Integer usesAllowed;

    @Column(name = "uses_remaining")
    private Integer usesRemaining;

    @Column(name = "valid_from", nullable = false)
    private LocalDateTime validFrom;

    @Column(name = "valid_until", nullable = false)
    private LocalDateTime validUntil;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PassStatus status = PassStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Raw FK -- authoritative. Set server-side from the resident's JWT
    // societyId claim (see PassService.createPass), never trusted from the
    // client, and never overridable via the relationship (insertable /
    // updatable = false below).
    @Column(name = "society_id", nullable = false)
    private Long societyId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "society_id", insertable = false, updatable = false)
    private Society society;

    @Column(name = "last_used_date")
    private LocalDate lastUsedDate;

    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "visitorPass", fetch = FetchType.LAZY)
    private List<EntryLog> entryLogs = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
