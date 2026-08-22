package com.pravesh.repository;

import com.pravesh.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findTopByUserIdAndIsUsedFalseOrderByCreatedAtDesc(Long userId);
}