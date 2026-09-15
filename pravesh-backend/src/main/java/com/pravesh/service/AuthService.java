package com.pravesh.service;

import com.pravesh.dto.request.*;
import com.pravesh.dto.response.AuthResponse;
import com.pravesh.entity.*;
import com.pravesh.entity.enums.Role;
import com.pravesh.entity.enums.VerificationStatus;
import com.pravesh.exception.*;
import com.pravesh.repository.*;
import com.pravesh.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final ResidentRepository residentRepository;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final com.pravesh.service.NotificationService notificationService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final SocietyAdminRepository societyAdminRepository;
    private final GuardRepository guardRepository;
    private final RegistrationVerificationRepository registrationVerificationRepository;

    private static final SecureRandom RANDOM = new SecureRandom();

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        String requestedRole = (req.role() == null || req.role().isBlank())
                ? "RESIDENT" : req.role().toUpperCase();

        if (!requestedRole.equals("RESIDENT") && !requestedRole.equals("SOCIETY_ADMIN")) {
            throw new InvalidStateException(
                    "Self-registration is only permitted for residents and society admins");
        }

        if (userRepository.existsByEmail(req.email())) {
            throw new DuplicateResourceException("Email already registered");
        }
        if (userRepository.existsByPhone(req.phone())) {
            throw new DuplicateResourceException("Phone number already registered");
        }

        // Both contacts must have been OTP-verified via /api/auth/register/send-otp
        // + /api/auth/register/verify-otp before an account can actually be created.
        RegistrationVerification emailVerification = registrationVerificationRepository
                .findTopByContactTypeAndContactValueAndVerifiedTrueAndConsumedFalseOrderByCreatedAtDesc(
                        "EMAIL", req.email())
                .orElseThrow(() -> new InvalidStateException(
                        "Please verify your email before registering."));
        RegistrationVerification phoneVerification = registrationVerificationRepository
                .findTopByContactTypeAndContactValueAndVerifiedTrueAndConsumedFalseOrderByCreatedAtDesc(
                        "PHONE", req.phone())
                .orElseThrow(() -> new InvalidStateException(
                        "Please verify your phone number before registering."));

        Role role = Role.valueOf(requestedRole);

        User user = User.builder()
                .name(req.name())
                .email(req.email())
                .phone(req.phone())
                .passwordHash(passwordEncoder.encode(req.password()))
                .role(role)
                .state(req.state())
                .isActive(true)
                .build();
        user = userRepository.save(user);

        String verificationStatus;

        if (role == Role.RESIDENT) {
            Resident resident = Resident.builder()
                    .user(user)
                    .flat(null)
                    .verificationStatus(VerificationStatus.PENDING)
                    .build();
            residentRepository.save(resident);
            verificationStatus = resident.getVerificationStatus().name();
        } else {
            SocietyAdmin admin = SocietyAdmin.builder()
                    .user(user)
                    .society(null)
                    .verificationStatus(VerificationStatus.PENDING)
                    .build();
            societyAdminRepository.save(admin);
            verificationStatus = admin.getVerificationStatus().name();
        }

        emailVerification.setConsumed(true);
        phoneVerification.setConsumed(true);
        registrationVerificationRepository.save(emailVerification);
        registrationVerificationRepository.save(phoneVerification);

        String token = jwtUtil.generateToken(user, verificationStatus, null);

        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail(),
                user.getRole().name(), verificationStatus);
    }

    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }
        if (!user.isActive()) {
            throw new InvalidStateException("This account has been deactivated");
        }

        String verificationStatus = null;
        Long societyId = null;

        if (user.getRole() == Role.RESIDENT) {
            var resident = residentRepository.findById(user.getId()).orElse(null);
            if (resident != null) {
                verificationStatus = resident.getVerificationStatus().name();
                Flat flat = resident.getFlat();
                if (flat != null && flat.getSociety() != null) {
                    societyId = flat.getSociety().getId();
                }
            }
        } else if (user.getRole() == Role.SOCIETY_ADMIN) {
            var admin = societyAdminRepository.findById(user.getId()).orElse(null);
            if (admin != null) {
                verificationStatus = admin.getVerificationStatus().name();
                societyId = admin.getSociety() != null ? admin.getSociety().getId() : null;
            }
        } else if (user.getRole() == Role.GUARD) {
            var guard = guardRepository.findById(user.getId()).orElse(null);
            if (guard != null && guard.getGate() != null && guard.getGate().getSociety() != null) {
                societyId = guard.getGate().getSociety().getId();
            }
        }

        String token = jwtUtil.generateToken(user, verificationStatus, societyId);

        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail(),
                user.getRole().name(), verificationStatus);
    }
    @Transactional
    public void forgotPassword(ForgotPasswordRequest req) {
        User user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No account found with that email"));

        String channel = (req.channel() == null || req.channel().isBlank())
                ? "BOTH" : req.channel().toUpperCase();

        String otp = String.format("%06d", RANDOM.nextInt(1_000_000));
        String otpHash = passwordEncoder.encode(otp);
        log.info("DEV ONLY - forgot-password OTP for {}: {}", user.getEmail(), otp);

        PasswordResetToken tokenEntity = PasswordResetToken.builder()
                .user(user)
                .otpHash(otpHash)
                .channel(channel)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .isUsed(false)
                .attemptCount(0)
                .build();
        resetTokenRepository.save(tokenEntity);

        String correlationId = UUID.randomUUID().toString();
        notificationService.handleOtpRequested(correlationId, user.getId(), user.getEmail(),
                user.getPhone(), otp, channel, "PASSWORD_RESET");
    }

    @Transactional
    public String verifyOtp(VerifyOtpRequest req) {
        User user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new ResourceNotFoundException("No account found with that email"));

        PasswordResetToken tokenEntity = resetTokenRepository
                .findTopByUser_IdAndIsUsedFalseOrderByCreatedAtDesc(user.getId())
                .orElseThrow(() -> new OtpValidationException("No active OTP request found"));

        if (tokenEntity.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new OtpValidationException("OTP has expired. Please request a new one.");
        }
        if (tokenEntity.getAttemptCount() >= 5) {
            throw new OtpValidationException("Too many incorrect attempts. Please request a new OTP.");
        }
        if (!passwordEncoder.matches(req.otp(), tokenEntity.getOtpHash())) {
            tokenEntity.setAttemptCount(tokenEntity.getAttemptCount() + 1);
            resetTokenRepository.save(tokenEntity);
            throw new OtpValidationException("Incorrect OTP");
        }

        return jwtUtil.generateResetToken(user.getId(), tokenEntity.getId());
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest req) {
        Long[] ids = jwtUtil.parseResetToken(req.resetToken());
        Long userId = ids[0];
        Long tokenId = ids[1];

        PasswordResetToken tokenEntity = resetTokenRepository.findById(tokenId)
                .orElseThrow(() -> new OtpValidationException("Invalid or expired reset token"));

        if (tokenEntity.isUsed()) {
            throw new OtpValidationException("This reset token has already been used");
        }
        if (!tokenEntity.getUser().getId().equals(userId)) {
            throw new OtpValidationException("Invalid reset token");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        userRepository.save(user);

        tokenEntity.setUsed(true);
        resetTokenRepository.save(tokenEntity);
    }
}