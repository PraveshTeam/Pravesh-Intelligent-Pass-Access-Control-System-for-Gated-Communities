package com.pravesh.dto.response;

public record PassValidationResponse(
        boolean granted,
        String reason,           // null if granted; QR_INVALID / QR_NOT_YET_ACTIVE / QR_EXPIRED / REVOKED / ALREADY_USED
        Long passId,
        Long residentId,
        String visitorName,
        String passType
) {
    public static PassValidationResponse denied(String reason) {
        return new PassValidationResponse(false, reason, null, null, null, null);
    }

    public static PassValidationResponse granted(Long passId, Long residentId,
                                                   String visitorName, String passType) {
        return new PassValidationResponse(true, null, passId, residentId, visitorName, passType);
    }
}