package com.pravesh.service;

import com.pravesh.dto.request.SendRegistrationOtpRequest;
import com.pravesh.dto.request.VerifyRegistrationOtpRequest;
import com.pravesh.entity.RegistrationVerification;
import com.pravesh.exception.DuplicateResourceException;
import com.pravesh.exception.InvalidStateException;
import com.pravesh.exception.OtpValidationException;
import com.pravesh.repository.RegistrationVerificationRepository;
import com.pravesh.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrationVerificationService {

    private final UserRepository userRepository;
    private final RegistrationVerificationRepository verificationRepository;
    private final com.pravesh.service.NotificationService notificationService;
    private final PasswordEncoder passwordEncoder;

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[6-9]\\d{9}$");

    @Transactional
    public void sendOtp(SendRegistrationOtpRequest req) {
        String type = normalizeType(req.contactType());
        String value = req.value() == null ? "" : req.value().trim();

        if (type.equals("EMAIL")) {
            if (!EMAIL_PATTERN.matcher(value).matches()) {
                throw new InvalidStateException("Enter a valid email address");
            }
            if (userRepository.existsByEmail(value)) {
                throw new DuplicateResourceException("Email already registered");
            }
        } else {
            if (!PHONE_PATTERN.matcher(value).matches()) {
                throw new InvalidStateException("Enter a valid 10-digit phone number");
            }
            if (userRepository.existsByPhone(value)) {
                throw new DuplicateResourceException("Phone number already registered");
            }
        }

        String otp = String.format("%06d", RANDOM.nextInt(1_000_000));
        String otpHash = passwordEncoder.encode(otp);
        log.info("DEV ONLY - registration OTP for {}: {}", value, otp);

        RegistrationVerification verification = RegistrationVerification.builder()
                .contactType(type)
                .contactValue(value)
                .otpHash(otpHash)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .verified(false)
                .consumed(false)
                .attemptCount(0)
                .build();
        verification = verificationRepository.save(verification);

        // Same NotificationService method the forgot-password OTP uses — used
        // to go via an outbox row + RabbitMQ queue, now it's a direct call.
        // userId is null since no account exists yet.
        String correlationId = UUID.randomUUID().toString();
        notificationService.handleOtpRequested(correlationId, null,
                type.equals("EMAIL") ? value : null,
                type.equals("PHONE") ? value : null,
                otp, type.equals("EMAIL") ? "EMAIL" : "SMS", "REGISTRATION_VERIFICATION");
    }

    @Transactional
    public void verifyOtp(VerifyRegistrationOtpRequest req) {
        String type = normalizeType(req.contactType());
        String value = req.value() == null ? "" : req.value().trim();

        RegistrationVerification verification = verificationRepository
                .findTopByContactTypeAndContactValueAndConsumedFalseOrderByCreatedAtDesc(type, value)
                .orElseThrow(() -> new OtpValidationException(
                        "No active OTP request found. Please send a code first."));

        if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new OtpValidationException("OTP has expired. Please request a new one.");
        }
        if (verification.getAttemptCount() >= 5) {
            throw new OtpValidationException("Too many incorrect attempts. Please request a new OTP.");
        }
        if (!passwordEncoder.matches(req.otp(), verification.getOtpHash())) {
            verification.setAttemptCount(verification.getAttemptCount() + 1);
            verificationRepository.save(verification);
            throw new OtpValidationException("Incorrect OTP");
        }

        verification.setVerified(true);
        verificationRepository.save(verification);
    }

    private String normalizeType(String contactType) {
        String type = contactType == null ? "" : contactType.trim().toUpperCase();
        if (!type.equals("EMAIL") && !type.equals("PHONE")) {
            throw new InvalidStateException("contactType must be EMAIL or PHONE");
        }
        return type;
    }
}