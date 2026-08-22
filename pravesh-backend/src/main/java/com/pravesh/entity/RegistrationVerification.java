package com.pravesh.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Tracks OTP verification of an email or phone number BEFORE the account
 * actually exists — used by the "verify email/phone during registration"
 * flow. Reuses the exact same OTP generation / hashing / OTP-dispatch
 * delivery pattern as {@link PasswordResetToken} (forgot-password), just
 * keyed by the raw contact value instead of a userId, since no user row
 * exists yet at this point.
 */
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

    /** The raw email address or phone number being verified. */
    @Column(name = "contact_value", nullable = false, length = 150)
    private String contactValue;

    @Column(name = "otp_hash", nullable = false)
    private String otpHash;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /** True once the correct OTP has been submitted. */
    @Column(nullable = false)
    @Builder.Default
    private boolean verified = false;

    /** True once this verification has been used to complete a registration (prevents replay). */
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
