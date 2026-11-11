package com.cjy.bikesystem.web.dto;

public record ViolationRequest(
        String bikeId,
        String reporterId,
        String violationPosition,
        String violationType
) {
}
