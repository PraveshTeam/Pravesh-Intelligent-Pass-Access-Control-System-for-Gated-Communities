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

// Separate from PaymentController: called by Razorpay's servers, not the
// frontend, and authenticated by HMAC signature rather than a JWT.
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

        // Signature covers the EXACT raw bytes -- never reformat before this check.
        boolean valid = razorpayGatewayService.verifyWebhookSignature(rawBody, signature);
        if (!valid) {
            throw new WebhookVerificationException("Webhook signature verification failed");
        }

        // Unknown events are acknowledged with 200 so Razorpay stops retrying.
        if (rawBody.contains("\"event\":\"payment.captured\"")) {
            paymentService.handlePaymentCaptured(rawBody);
        } else {
            log.info("Ignoring unhandled Razorpay webhook event (not payment.captured)");
        }

        return ResponseEntity.ok("OK");
    }
}
