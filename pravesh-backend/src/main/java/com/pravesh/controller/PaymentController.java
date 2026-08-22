package com.pravesh.controller;

import com.pravesh.dto.request.CreatePaymentOrderRequest;
import com.pravesh.dto.response.ApiResponse;
import com.pravesh.dto.response.CheckoutConfigResponse;
import com.pravesh.dto.response.PaymentOrderResponse;
import com.pravesh.security.AuthenticatedUser;
import com.pravesh.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
<<<<<<< HEAD
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
=======
>>>>>>> origin/salonee
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
<<<<<<< HEAD
    public ResponseEntity<ApiResponse<CheckoutConfigResponse>> createOrder(
=======
    public ApiResponse<CheckoutConfigResponse> createOrder(
>>>>>>> origin/salonee
            @AuthenticationPrincipal AuthenticatedUser caller,
            @Valid @RequestBody CreatePaymentOrderRequest req) {
        // societyId comes from the caller's own JWT claim (via the gateway's
        // X-Society-Id header) -- never from the request body, so a resident
        // can't spoof which society their payment gets tagged under.
<<<<<<< HEAD
        CheckoutConfigResponse response = paymentService.createOrder(req, caller.userId(), caller.societyId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Order created", response));
=======
        return ApiResponse.ok("Order created",
                paymentService.createOrder(req, caller.userId(), caller.societyId()));
>>>>>>> origin/salonee
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('RESIDENT')")
<<<<<<< HEAD
    public ResponseEntity<ApiResponse<List<PaymentOrderResponse>>> myHistory(@AuthenticationPrincipal AuthenticatedUser caller) {
        return ResponseEntity.ok(ApiResponse.ok("Payment history", paymentService.myHistory(caller.userId())));
=======
    public ApiResponse<List<PaymentOrderResponse>> myHistory(@AuthenticationPrincipal AuthenticatedUser caller) {
        return ApiResponse.ok("Payment history", paymentService.myHistory(caller.userId()));
>>>>>>> origin/salonee
    }

    @GetMapping("/admin/payments")
    @PreAuthorize("hasRole('SOCIETY_ADMIN')")
<<<<<<< HEAD
    public ResponseEntity<ApiResponse<List<PaymentOrderResponse>>> allPayments(
=======
    public ApiResponse<List<PaymentOrderResponse>> allPayments(
>>>>>>> origin/salonee
            @AuthenticationPrincipal AuthenticatedUser caller,
            @RequestParam(required = false) String purpose,
            @RequestParam(required = false) String status) {
        // Scoped to the admin's OWN society -- this is the fix for the
        // cross-society data leak. caller.societyId() comes from the JWT,
        // an admin cannot pass a different society's ID to see its payments.
<<<<<<< HEAD
        return ResponseEntity.ok(ApiResponse.ok("All payments",
                paymentService.allPayments(purpose, status, caller.societyId())));
=======
        return ApiResponse.ok("All payments",
                paymentService.allPayments(purpose, status, caller.societyId()));
>>>>>>> origin/salonee
    }
}
