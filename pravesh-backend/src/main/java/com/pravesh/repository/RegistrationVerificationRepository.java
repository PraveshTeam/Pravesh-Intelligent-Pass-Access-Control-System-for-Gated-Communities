package com.pravesh.repository;

import com.pravesh.entity.RegistrationVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RegistrationVerificationRepository extends JpaRepository<RegistrationVerification, Long> {

    /** Most recent, not-yet-consumed verification attempt for a given contact — used while verifying an OTP. */
    Optional<RegistrationVerification> findTopByContactTypeAndContactValueAndConsumedFalseOrderByCreatedAtDesc(
            String contactType, String contactValue);

    /** Most recent successfully-verified, not-yet-consumed row — used to gate the actual /register call. */
    Optional<RegistrationVerification> findTopByContactTypeAndContactValueAndVerifiedTrueAndConsumedFalseOrderByCreatedAtDesc(
            String contactType, String contactValue);
}
