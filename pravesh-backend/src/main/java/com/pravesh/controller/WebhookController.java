package com.pravesh.controller;

import com.pravesh.exception.WebhookVerificationException;
import com.pravesh.service.PaymentService;
import com.pravesh.service.RazorpayGatewayService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

// Deliberately a SEPARATE controller from PaymentController: this endpoint is
// called by Razorpay's servers, not the frontend, is exempted from the header
// auth filter's normal JWT-derived headers, and is authenticated purely by its
// HMAC signature — a completely different trust model from every other endpoint
// in this service, so it earns its own class.
@RestController
@RequiredArgsConstructor
public class WebhookController {

    private static final Logger log = LoggerFactory.getLogger(WebhookController.class);

    private final RazorpayGatewayService razorpayGatewayService;
    private final PaymentService paymentService;

    @PostMapping("/api/payments/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String rawBody,
            @RequestHeader("X-Razorpay-Signature") String signature) {

        // The signature is computed over the EXACT raw bytes Razorpay sent.
        // Never re-serialize/re-parse-then-re-stringify before this check —
        // any reformatting changes the bytes and breaks the signature match.
        boolean valid = razorpayGatewayService.verifyWebhookSignature(rawBody, signature);
        if (!valid) {
            throw new WebhookVerificationException("Webhook signature verification failed");
        }

        // Only handle payment.captured for now — payment.failed etc. can be
        // added the same way once needed. Unknown/irrelevant events are
        // acknowledged with 200 so Razorpay doesn't keep retrying them forever.
        if (rawBody.contains("\"event\":\"payment.captured\"")) {
            paymentService.handlePaymentCaptured(rawBody);
        } else {
            log.info("Ignoring unhandled Razorpay webhook event (not payment.captured)");
        }

        return ResponseEntity.ok("OK");
    }
}
