package com.pravesh.dto.request;

public record RelocationApprovedRequest(
        Long residentId,
        String newFlatNumber,
        String newTower,
        String newSocietyName,
        String oldFlatNumber,
        String oldSocietyName
) {}