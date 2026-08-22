package com.pravesh.service;

import com.pravesh.dto.request.CreatePaymentOrderRequest;
import com.pravesh.dto.response.CheckoutConfigResponse;
import com.pravesh.dto.response.PaymentOrderResponse;
import com.pravesh.entity.PaymentOrder;
import com.pravesh.entity.Resident;
import com.pravesh.entity.Society;
import com.pravesh.entity.PaymentPurpose;
import com.pravesh.entity.PaymentStatus;
import com.pravesh.exception.InvalidStateException;
import com.pravesh.exception.ResourceNotFoundException;
import com.pravesh.dto.response.ResidentContextResponse;
import com.pravesh.repository.PaymentOrderRepository;
import com.pravesh.util.EntityRefs;
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
    private final EntityRefs refs;

    @Transactional
    public CheckoutConfigResponse createOrder(CreatePaymentOrderRequest req, Long residentId, Long societyId) {
        if (societyId == null) {
            // Fail loudly rather than saving an order with no tenant.
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

        // Call Razorpay FIRST so the entity is saved once, fully populated
        // (razorpay_order_id is NOT NULL).
        String receiptRef = "pravesh-" + residentId + "-" + System.currentTimeMillis();
        String razorpayOrderId;
        try {
            razorpayOrderId = razorpayGatewayService.createOrder(req.amount(), receiptRef);
        } catch (RazorpayException e) {
            log.error("Razorpay order creation failed for resident {}: {}", residentId, e.getMessage());
            throw new InvalidStateException("Could not initiate payment. Please try again.");
        }

        PaymentOrder order = PaymentOrder.builder()
                .resident(refs.ref(Resident.class, residentId))
                .society(refs.ref(Society.class, societyId))
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

    // A resident viewing their own history doesn't need name/flat enrichment.
    public List<PaymentOrderResponse> myHistory(Long residentId) {
        return orderRepository.findByResident_UserIdOrderByCreatedAtDesc(residentId)
                .stream().map(o -> toResponse(o, null)).toList();
    }

    // Scoped to the calling admin's OWN society (adminSocietyId comes from the
    // JWT, never from a request param) -- this is the cross-tenant leak fix.
    public List<PaymentOrderResponse> allPayments(String purpose, String status, Long adminSocietyId) {
        List<PaymentOrder> orders = orderRepository.findBySocietyIdOrderByCreatedAtDesc(adminSocietyId).stream()
                .filter(o -> purpose == null || o.getPurpose().name().equalsIgnoreCase(purpose))
                .filter(o -> status == null || o.getStatus().name().equalsIgnoreCase(status))
                .toList();

        Set<Long> distinctResidentIds = orders.stream()
                .map(o -> o.getResident().getUserId())
                .collect(Collectors.toSet());

        Map<Long, ResidentContextResponse> residentContextById = new HashMap<>();
        for (Long residentId : distinctResidentIds) {
            try {
                ResidentContextResponse ctx = userDirectoryService.getResidentContext(residentId);
                if (ctx != null) {
                    residentContextById.put(residentId, ctx);
                }
            } catch (Exception e) {
                // One failed lookup shouldn't break the whole admin listing.
                log.warn("Could not resolve resident context for {} while building admin payment list: {}",
                        residentId, e.getMessage(), e);
            }
        }

        return orders.stream()
                .map(o -> toResponse(o, residentContextById.get(o.getResident().getUserId())))
                .toList();
    }

    // Signature is verified before this is called (see WebhookController).
    // Idempotent: Razorpay retries must never double-process a receipt.
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
        try {
            notificationService.handlePaymentReceipt(order.getId(), order.getResident().getUserId(),
                    order.getAmount().doubleValue(), order.getPurpose().name(), order.getPaidAt());
        } catch (Exception e) {
            log.error("Failed to dispatch payment receipt for order {}: {}", order.getId(), e.getMessage());
        }
    }

    private PaymentOrderResponse toResponse(PaymentOrder o, ResidentContextResponse ctx) {
        return new PaymentOrderResponse(
                o.getId(),
                o.getResident().getUserId(),
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
