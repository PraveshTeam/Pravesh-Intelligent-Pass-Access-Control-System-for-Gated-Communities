package com.pravesh.service;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

// Thin wrapper around the Razorpay SDK.
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

    /** Creates a Razorpay order and returns its id. Amount is sent in paise. */
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

    /** Verifies the webhook's HMAC-SHA256 signature. Never skip this. */
    public boolean verifyWebhookSignature(String rawBody, String signature) {
        try {
            return Utils.verifyWebhookSignature(rawBody, signature, webhookSecret);
        } catch (RazorpayException e) {
            return false;
        }
    }
}
