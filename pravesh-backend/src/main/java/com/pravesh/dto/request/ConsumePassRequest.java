package com.pravesh.dto.request;

public record ConsumePassRequest(
        boolean setConsumed // true for ONE_TIME or last MULTI_USE, false to just decrement
) {}