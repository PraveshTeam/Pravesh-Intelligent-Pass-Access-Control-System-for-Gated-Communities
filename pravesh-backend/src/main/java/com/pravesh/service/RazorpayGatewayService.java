package com.pravesh.service;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

// Thin wrapper around the Razorpay Java SDK — isolates the third-party
// dependency so PaymentService never touches RazorpayClient directly.
@Service
public class RazorpayGatewayService {

    @Value("${razorpay.key-id}")
    private String keyId;

    @Value("${razorpay.key-secret}")
    private String keySecret;

    @Value("${razorpay.webhook-secret}")
    private String webhookSecret;

    private RazorpayClient client() throws RazorpayException {
        return new RazorpayClient(keyId, keySecret);
    }

    public String getKeyId() {
        return keyId;
    }

    /**
     * Creates a Razorpay order and returns its order id (e.g. "order_xyz").
     * Amount must be in paise (smallest currency unit) — Razorpay's API never
     * accepts rupees directly, so ₹500.00 becomes 50000.
     */
    public String createOrder(BigDecimal amountInRupees, String receiptId) throws RazorpayException {
        long amountInPaise = amountInRupees.multiply(BigDecimal.valueOf(100)).longValueExact();

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amountInPaise);
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", receiptId);
        orderRequest.put("payment_capture", 1); // auto-capture on successful payment

        com.razorpay.Order order = client().orders.create(orderRequest);
        return order.get("id");
    }

    /**
     * Verifies a webhook's HMAC-SHA256 signature against the webhook secret.
     * This is the ONLY thing that makes a webhook trustworthy — the payload
     * itself is just JSON anyone could POST. Never mark an order PAID without
     * this check passing first.
     *
     * @param rawBody   the exact, unmodified request body Razorpay sent
     * @param signature the value of the "X-Razorpay-Signature" header
     * @return true if the signature is valid for this exact body + our webhook secret
     */
    public boolean verifyWebhookSignature(String rawBody, String signature) {
        try {
            return Utils.verifyWebhookSignature(rawBody, signature, webhookSecret);
        } catch (RazorpayException e) {
            return false;
        }
    }
}
