package com.pravesh.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// OTP verification of an email/phone BEFORE the account exists -- keyed by the
// raw contact value, since no user row exists yet.
@Entity
@Table(name = "registration_verifications")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class RegistrationVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** "EMAIL" or "PHONE" */
    @Column(name = "contact_type", nullable = false, length = 10)
    private String contactType;

    @Column(name = "contact_value", nullable = false, length = 150)
    private String contactValue;

    @Column(name = "otp_hash", nullable = false)
    private String otpHash;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean verified = false;

    /** True once used to complete a registration (prevents replay). */
    @Column(nullable = false)
    @Builder.Default
    private boolean consumed = false;

    @Column(name = "attempt_count", nullable = false)
    @Builder.Default
    private int attemptCount = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
