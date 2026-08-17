package com.pravesh.controller;

import com.pravesh.dto.request.CreatePaymentOrderRequest;
import com.pravesh.dto.response.ApiResponse;
import com.pravesh.dto.response.CheckoutConfigResponse;
import com.pravesh.dto.response.PaymentOrderResponse;
import com.pravesh.security.AuthenticatedUser;
import com.pravesh.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/orders")
    @PreAuthorize("hasRole('RESIDENT')")
    public ResponseEntity<ApiResponse<CheckoutConfigResponse>> createOrder(
            @AuthenticationPrincipal AuthenticatedUser caller,
            @Valid @RequestBody CreatePaymentOrderRequest req) {
        // societyId comes from the caller's own JWT claim (via the gateway's
        // X-Society-Id header) -- never from the request body, so a resident
        // can't spoof which society their payment gets tagged under.
        CheckoutConfigResponse response = paymentService.createOrder(req, caller.userId(), caller.societyId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Order created", response));
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('RESIDENT')")
    public ResponseEntity<ApiResponse<List<PaymentOrderResponse>>> myHistory(@AuthenticationPrincipal AuthenticatedUser caller) {
        return ResponseEntity.ok(ApiResponse.ok("Payment history", paymentService.myHistory(caller.userId())));
    }

    @GetMapping("/admin/payments")
    @PreAuthorize("hasRole('SOCIETY_ADMIN')")
    public ResponseEntity<ApiResponse<List<PaymentOrderResponse>>> allPayments(
            @AuthenticationPrincipal AuthenticatedUser caller,
            @RequestParam(required = false) String purpose,
            @RequestParam(required = false) String status) {
        // Scoped to the admin's OWN society -- this is the fix for the
        // cross-society data leak. caller.societyId() comes from the JWT,
        // an admin cannot pass a different society's ID to see its payments.
        return ResponseEntity.ok(ApiResponse.ok("All payments",
                paymentService.allPayments(purpose, status, caller.societyId())));
    }
}
