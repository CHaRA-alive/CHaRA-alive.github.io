package com.cjy.bikesystem.web.dto;

public record MaintenanceRequest(
        String bikeId,
        String reporterId,
        String desc,
        String description,
        String workType
) {
    public String resolvedDescription() {
        return desc == null || desc.isBlank() ? description : desc;
    }
}
