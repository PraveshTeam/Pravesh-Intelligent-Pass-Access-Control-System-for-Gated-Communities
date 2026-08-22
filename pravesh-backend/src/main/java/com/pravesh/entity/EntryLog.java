package com.pravesh.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pravesh.entity.enums.EntryType;
import com.pravesh.entity.enums.ScanResult;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "entry_logs")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class EntryLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pass_id")
    private VisitorPass visitorPass;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false, length = 10)
    @Builder.Default
    private EntryType entryType = EntryType.QR_PASS;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resident_id")
    private Resident resident;

    @Column(name = "visitor_name", length = 100)
    private String visitorName;

    // Set server-side from the authenticated guard's principal, never from the request body.
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guard_id")
    private Guard guard;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gate_id")
    private Gate gate;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_id")
    private GuardShift shift;

    @Enumerated(EnumType.STRING)
    @Column(name = "scan_result", nullable = false, length = 10)
    private ScanResult scanResult;

    @Column(name = "deny_reason", length = 30)
    private String denyReason;

    @Column(name = "scanned_at", nullable = false, updatable = false)
    private LocalDateTime scannedAt;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "society_id")
    private Society society;

    @PrePersist
    protected void onCreate() {
        this.scannedAt = LocalDateTime.now();
    }
}
