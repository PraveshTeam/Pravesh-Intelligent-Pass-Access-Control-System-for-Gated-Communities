package com.pravesh.repository;

import com.pravesh.entity.PaymentOrder;
import com.pravesh.entity.PaymentPurpose;
import com.pravesh.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, Long> {

    Optional<PaymentOrder> findByRazorpayOrderId(String razorpayOrderId);

    List<PaymentOrder> findByResidentIdOrderByCreatedAtDesc(Long residentId);

    List<PaymentOrder> findByPurposeAndStatusOrderByCreatedAtDesc(PaymentPurpose purpose, PaymentStatus status);

    // Scoped to the admin's own society -- this is what fixes the cross-society
    // data leak (an admin from one society was able to see every society's payments).
    List<PaymentOrder> findBySocietyIdOrderByCreatedAtDesc(Long societyId);
}
