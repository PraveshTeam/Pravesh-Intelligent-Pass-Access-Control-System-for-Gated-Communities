package com.pravesh.controller;

import com.pravesh.dto.request.*;
import com.pravesh.dto.response.ApiResponse;
import com.pravesh.dto.response.AuthResponse;
import com.pravesh.service.AuthService;
import com.pravesh.service.RegistrationVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RegistrationVerificationService registrationVerificationService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest req) {
        AuthResponse response = authService.register(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Registration successful. Please complete onboarding.", response));
    }

    // ── Email/Phone verification during registration (reuses the forgot-password OTP flow) ──

    @PostMapping("/register/send-otp")
    public ResponseEntity<ApiResponse<Void>> sendRegistrationOtp(@Valid @RequestBody SendRegistrationOtpRequest req) {
        registrationVerificationService.sendOtp(req);
        String channel = "EMAIL".equalsIgnoreCase(req.contactType()) ? "email" : "phone";
        return ResponseEntity.ok(ApiResponse.ok("OTP sent to your " + channel + "."));
    }

    @PostMapping("/register/verify-otp")
    public ResponseEntity<ApiResponse<Void>> verifyRegistrationOtp(@Valid @RequestBody VerifyRegistrationOtpRequest req) {
        registrationVerificationService.verifyOtp(req);
        String channel = "EMAIL".equalsIgnoreCase(req.contactType()) ? "Email" : "Phone";
        return ResponseEntity.ok(ApiResponse.ok(channel + " verified successfully."));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Login successful", authService.login(req)));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest req) {
        authService.forgotPassword(req);
        return ResponseEntity.ok(ApiResponse.ok("If an account exists for that email, an OTP has been sent."));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<Map<String, String>>> verifyOtp(@Valid @RequestBody VerifyOtpRequest req) {
        String resetToken = authService.verifyOtp(req);
        return ResponseEntity.ok(ApiResponse.ok("OTP verified", Map.of("resetToken", resetToken)));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        authService.resetPassword(req);
        return ResponseEntity.ok(ApiResponse.ok("Password reset successful. Please log in with your new password."));
    }
}
