package com.pravesh.repository;

import com.pravesh.entity.GuardShift;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GuardShiftRepository extends JpaRepository<GuardShift, Long> {

    Optional<GuardShift> findTopByGuard_UserIdAndShiftEndIsNullOrderByShiftStartDesc(Long guardUserId);

    List<GuardShift> findByGuard_UserIdOrderByShiftStartDesc(Long guardUserId);

    Optional<GuardShift> findTopByGateIdAndShiftEndIsNullOrderByShiftStartDesc(Long gateId);
}
