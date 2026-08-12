package com.pravesh.exception;

// Thrown when a Razorpay webhook's signature does not match — never trust
// the payload if this fails, regardless of what the body claims.
public class WebhookVerificationException extends RuntimeException {
    public WebhookVerificationException(String message) {
        super(message);
    }
}
