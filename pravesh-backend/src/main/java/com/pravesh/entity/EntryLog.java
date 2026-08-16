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

    @Column(name = "pass_id")
    private Long passId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pass_id", insertable = false, updatable = false)
    private VisitorPass visitorPass;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false, length = 10)
    @Builder.Default
    private EntryType entryType = EntryType.QR_PASS;

    @Column(name = "resident_id")
    private Long residentId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resident_id", insertable = false, updatable = false)
    private Resident resident;

    @Column(name = "visitor_name", length = 100)
    private String visitorName;

    // Written server-side only from the authenticated guard's own principal
    // (SecurityContext), never from the scan request body -- kept read-only
    // on the relationship for the same reason.
    @Column(name = "guard_id")
    private Long guardId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guard_id", insertable = false, updatable = false)
    private Guard guard;

    @Column(name = "gate_id")
    private Long gateId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gate_id", insertable = false, updatable = false)
    private Gate gate;

    @Column(name = "shift_id")
    private Long shiftId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_id", insertable = false, updatable = false)
    private GuardShift shift;

    @Enumerated(EnumType.STRING)
    @Column(name = "scan_result", nullable = false, length = 10)
    private ScanResult scanResult;

    @Column(name = "deny_reason", length = 30)
    private String denyReason;

    @Column(name = "scanned_at", nullable = false, updatable = false)
    private LocalDateTime scannedAt;

    // Raw FK -- authoritative. Set server-side from the scanning guard's own
    // society (JWT), never trusted from the request -- this is the exact
    // multi-tenancy field called out in the PaymentOrder/Trip comments
    // elsewhere in this codebase.
    @Column(name = "society_id")
    private Long societyId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "society_id", insertable = false, updatable = false)
    private Society society;

    @PrePersist
    protected void onCreate() {
        this.scannedAt = LocalDateTime.now();
    }
}
