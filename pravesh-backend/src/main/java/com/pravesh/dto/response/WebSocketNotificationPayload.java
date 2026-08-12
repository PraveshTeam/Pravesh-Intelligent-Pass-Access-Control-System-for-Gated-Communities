package com.pravesh.dto.response;

public record WebSocketNotificationPayload(
        String type,
        String visitorName,
        String enteredAt,
        String gateName
) {}