package com.pravesh.exception;

import com.pravesh.dto.response.FlatConflictErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

// Single, application-wide exception handler.
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(f -> errors.put(f.getField(), f.getDefaultMessage()));
        return ResponseEntity.badRequest().body(
                new ApiErrorResponse("Validation failed", errors, req.getRequestURI()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(
            ResourceNotFoundException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ApiErrorResponse(ex.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicate(
            DuplicateResourceException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                new ApiErrorResponse(ex.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(InvalidStateException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidState(
            InvalidStateException ex, HttpServletRequest req) {
        return ResponseEntity.badRequest().body(
                new ApiErrorResponse(ex.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleBadCredentials(
            InvalidCredentialsException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new ApiErrorResponse(ex.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(OtpValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleOtp(
            OtpValidationException ex, HttpServletRequest req) {
        return ResponseEntity.badRequest().body(
                new ApiErrorResponse(ex.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(ShiftRequiredException.class)
    public ResponseEntity<ApiErrorResponse> handleShiftRequired(
            ShiftRequiredException ex, HttpServletRequest req) {
        return ResponseEntity.status(428).body( // 428 Precondition Required
                new ApiErrorResponse(ex.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(WebhookVerificationException.class)
    public ResponseEntity<ApiErrorResponse> handleWebhookVerification(
            WebhookVerificationException ex, HttpServletRequest req) {
        log.warn("Webhook signature verification failed at {}: {}", req.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new ApiErrorResponse(ex.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(FlatOccupiedException.class)
    public ResponseEntity<FlatConflictErrorResponse> handleFlatOccupied(FlatOccupiedException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                new FlatConflictErrorResponse(
                        false, ex.getMessage(), true,
                        ex.getOccupantResidentId(), ex.getOccupantName(), ex.getFlatNumber()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                new ApiErrorResponse("You do not have permission to perform this action",
                        req.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(
            Exception ex, HttpServletRequest req) {
        log.error("Unhandled exception at {}", req.getRequestURI(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ApiErrorResponse("Something went wrong. Please try again.",
                        req.getRequestURI()));
    }
}
