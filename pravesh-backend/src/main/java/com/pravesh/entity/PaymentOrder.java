package com.pravesh.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_orders")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class PaymentOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "resident_id", nullable = false)
    private Long residentId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resident_id", insertable = false, updatable = false)
    private Resident resident;

    // CRITICAL for multi-tenancy: without this, an admin's "all payments" view
    // has no way to be scoped to their own society, and leaks every society's
    // payment records to every admin. Set once, at order-creation time, from
    // the resident's own JWT societyId claim -- never trust a client-supplied
    // value. The relationship below is deliberately insertable/updatable =
    // false so it can ONLY ever be used for read-side joins, never as an
    // alternate write path that could bypass the JWT-derived value.
    @Column(name = "society_id", nullable = false)
    private Long societyId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "society_id", insertable = false, updatable = false)
    private Society society;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentPurpose purpose;

    // Polymorphic reference (trip_id, etc. depending on `purpose`) -- cannot
    // be a single typed JPA relationship since the target table varies.
    // Left as-is; resolve via the appropriate repository based on `purpose`.
    @Column(name = "reference_id")
    private Long referenceId; // e.g. trip_id if purpose = TRIP

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "razorpay_order_id", unique = true, nullable = false, length = 100)
    private String razorpayOrderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(name = "webhook_verified", nullable = false)
    private boolean webhookVerified;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.status = PaymentStatus.PENDING;
        this.webhookVerified = false;
    }
}
