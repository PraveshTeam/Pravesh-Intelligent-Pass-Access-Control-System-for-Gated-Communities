package com.pravesh.entity;

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

    // CRITICAL for multi-tenancy: without this, an admin's "all payments" view
    // has no way to be scoped to their own society, and leaks every society's
    // payment records to every admin. Set once, at order-creation time, from
    // the resident's own JWT societyId claim -- never trust a client-supplied value.
    @Column(name = "society_id", nullable = false)
    private Long societyId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentPurpose purpose;

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
