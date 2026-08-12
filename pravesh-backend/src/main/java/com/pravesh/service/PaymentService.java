package com.pravesh.service;

import com.pravesh.dto.request.CreatePaymentOrderRequest;
import com.pravesh.dto.response.CheckoutConfigResponse;
import com.pravesh.dto.response.PaymentOrderResponse;
import com.pravesh.entity.PaymentOrder;
import com.pravesh.entity.PaymentPurpose;
import com.pravesh.entity.PaymentStatus;
import com.pravesh.exception.InvalidStateException;
import com.pravesh.exception.ResourceNotFoundException;
import com.pravesh.dto.response.ResidentContextResponse;
import com.pravesh.repository.PaymentOrderRepository;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentOrderRepository orderRepository;
    private final com.pravesh.service.NotificationService notificationService;
    private final RazorpayGatewayService razorpayGatewayService;
    private final com.pravesh.service.UserDirectoryService userDirectoryService;

    @Transactional
    public CheckoutConfigResponse createOrder(CreatePaymentOrderRequest req, Long residentId, Long societyId) {
        if (societyId == null) {
            // Should never happen for a properly-onboarded RESIDENT (the gateway
            // injects X-Society-Id from the JWT), but fail loudly rather than
            // silently saving an order with no tenant, which would be unfilterable
            // and invisible to any admin's payment listing.
            throw new InvalidStateException("Could not determine your society. Please log in again.");
        }

        PaymentPurpose purpose;
        try {
            purpose = PaymentPurpose.valueOf(req.purpose().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidStateException("Unknown payment purpose: " + req.purpose());
        }

        if (purpose != PaymentPurpose.MAINTENANCE && req.referenceId() == null) {
            throw new InvalidStateException("referenceId is required for " + purpose + " payments");
        }

        // Call Razorpay FIRST -- its order id doesn't depend on our own DB id,
        // just a client-side reference string. This lets us save our entity
        // exactly once, fully populated, instead of an insert-then-update
        // (which would otherwise hit razorpay_order_id's NOT NULL constraint
        // on the first save, before we even know the value).
        String receiptRef = "pravesh-" + residentId + "-" + System.currentTimeMillis();
        String razorpayOrderId;
        try {
            razorpayOrderId = razorpayGatewayService.createOrder(req.amount(), receiptRef);
        } catch (RazorpayException e) {
            log.error("Razorpay order creation failed for resident {}: {}", residentId, e.getMessage());
            throw new InvalidStateException("Could not initiate payment. Please try again.");
        }

        PaymentOrder order = PaymentOrder.builder()
                .residentId(residentId)
                .societyId(societyId)
                .purpose(purpose)
                .referenceId(req.referenceId())
                .amount(req.amount())
                .razorpayOrderId(razorpayOrderId)
                .status(PaymentStatus.PENDING)
                .webhookVerified(false)
                .build();

        order = orderRepository.save(order);

        return new CheckoutConfigResponse(
                order.getId(),
                razorpayGatewayService.getKeyId(),
                razorpayOrderId,
                order.getAmount(),
                "INR");
    }

    // A resident viewing their own history already knows whose payments these
    // are -- no need to enrich with name/flat, and no need for the extra Feign calls.
    public List<PaymentOrderResponse> myHistory(Long residentId) {
        return orderRepository.findByResidentIdOrderByCreatedAtDesc(residentId)
                .stream().map(o -> toResponse(o, null)).toList();
    }

    // SECURITY-CRITICAL: scoped to the calling admin's OWN society. Without
    // this filter, any SOCIETY_ADMIN could see every society's payment records --
    // a genuine cross-tenant data leak, not just a cosmetic issue. adminSocietyId
    // comes from the caller's JWT (X-Society-Id header), never from a request param,
    // so an admin cannot simply pass a different societyId to view another society.
    public List<PaymentOrderResponse> allPayments(String purpose, String status, Long adminSocietyId) {
        List<PaymentOrder> orders = orderRepository.findBySocietyIdOrderByCreatedAtDesc(adminSocietyId).stream()
                .filter(o -> purpose == null || o.getPurpose().name().equalsIgnoreCase(purpose))
                .filter(o -> status == null || o.getStatus().name().equalsIgnoreCase(status))
                .toList();

        Set<Long> distinctResidentIds = orders.stream()
                .map(PaymentOrder::getResidentId)
                .collect(Collectors.toSet());

        Map<Long, ResidentContextResponse> residentContextById = new HashMap<>();
        for (Long residentId : distinctResidentIds) {
            try {
                ResidentContextResponse ctx = userDirectoryService.getResidentContext(residentId);
                if (ctx != null) {
                    residentContextById.put(residentId, ctx);
                }
            } catch (Exception e) {
                // A resident lookup failing (e.g. account since deactivated) shouldn't
                // break the whole admin listing -- that row just falls back to showing
                // the raw residentId instead of a name.
                log.warn("Could not resolve resident context for {} while building admin payment list: {}",
                        residentId, e.getMessage(), e);
            }
        }

        return orders.stream()
                .map(o -> toResponse(o, residentContextById.get(o.getResidentId())))
                .toList();
    }

    /**
     * Handles the Razorpay webhook. The signature MUST be verified before this
     * method is even called (see PaymentController) — by the time we're here,
     * the payload is trusted to have genuinely come from Razorpay.
     *
     * Idempotent: replays of the same webhook (Razorpay retries on slow/failed
     * responses) must never double-process or re-publish a second receipt event.
     */
    @Transactional
    public void handlePaymentCaptured(String rawBody) {
        JSONObject payload = new JSONObject(rawBody);
        JSONObject paymentEntity = payload
                .getJSONObject("payload")
                .getJSONObject("payment")
                .getJSONObject("entity");

        String razorpayOrderId = paymentEntity.getString("order_id");

        PaymentOrder order = orderRepository.findByRazorpayOrderId(razorpayOrderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No internal order found for Razorpay order " + razorpayOrderId));

        // Idempotency guard — Razorpay can and does retry webhook delivery.
        // If we've already marked this PAID, this is a safe no-op, not a
        // second SMS/email or a double-processed receipt.
        if (order.isWebhookVerified() && order.getStatus() == PaymentStatus.PAID) {
            log.info("Webhook for order {} already processed — skipping (idempotent no-op)", order.getId());
            return;
        }

        order.setStatus(PaymentStatus.PAID);
        order.setWebhookVerified(true);
        order.setPaidAt(LocalDateTime.now());
        orderRepository.save(order);

        notifyPaymentReceipt(order);
    }

    private void notifyPaymentReceipt(PaymentOrder order) {
        // Used to write an outbox row that a background poller published to
        // RabbitMQ; now it's a direct, synchronous call to NotificationService
        // (email receipt) right in the webhook request.
        try {
            notificationService.handlePaymentReceipt(order.getId(), order.getResidentId(),
                    order.getAmount().doubleValue(), order.getPurpose().name(), order.getPaidAt());
        } catch (Exception e) {
            log.error("Failed to dispatch payment receipt for order {}: {}", order.getId(), e.getMessage());
        }
    }

    private PaymentOrderResponse toResponse(PaymentOrder o, ResidentContextResponse ctx) {
        return new PaymentOrderResponse(
                o.getId(),
                o.getResidentId(),
                ctx != null ? ctx.name() : null,
                ctx != null ? ctx.flatNumber() : null,
                o.getPurpose(),
                o.getReferenceId(),
                o.getAmount(),
                o.getRazorpayOrderId(),
                o.getStatus(),
                o.getCreatedAt(),
                o.getPaidAt());
    }
}
