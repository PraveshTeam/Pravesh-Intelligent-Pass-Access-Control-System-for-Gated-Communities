package com.pravesh.security;

import com.pravesh.entity.User;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expirationMs;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms}") long expirationMs) {

        byte[] keyBytes;
        try {
            keyBytes = Base64.getDecoder().decode(secret);
        } catch (IllegalArgumentException e) {
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        }
        this.key = new SecretKeySpec(keyBytes, "HmacSHA256");
        this.expirationMs = expirationMs;
    }

    public String generateToken(User user, String verificationStatus) {
        return generateToken(user, verificationStatus, null);
    }

    public String generateToken(User user, String verificationStatus, Long societyId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        var builder = Jwts.builder()
                .subject(user.getEmail())
                .claim("userId", user.getId())
                .claim("role", user.getRole().name())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key);

        if (verificationStatus != null) {
            builder.claim("verificationStatus", verificationStatus);
        }
        if (societyId != null) {
            builder.claim("societyId", societyId);
        }

        return builder.compact();
    }

    public String generateResetToken(Long userId, Long tokenId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + 300_000);

        return Jwts.builder()
                .subject("password-reset")
                .claim("userId", userId)
                .claim("tokenId", tokenId)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    public Long[] parseResetToken(String token) {
        try {
            var claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Long userId = Long.valueOf(claims.get("userId").toString());
            Long tokenId = Long.valueOf(claims.get("tokenId").toString());
            return new Long[]{userId, tokenId};

        } catch (Exception e) {
            throw new com.pravesh.exception.OtpValidationException(
                    "Invalid or expired reset token");
        }
    }
}