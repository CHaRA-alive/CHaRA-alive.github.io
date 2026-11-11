package com.cjy.bikesystem.web.dto;

public record DispatchItem(
        String fromArea,
        String toArea,
        int bikeCount,
        double demandScore,
        long availableBikes,
        double demandGap,
        String priority,
        String reason
) {
}
