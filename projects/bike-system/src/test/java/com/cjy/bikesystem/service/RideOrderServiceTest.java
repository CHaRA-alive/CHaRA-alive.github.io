package com.cjy.bikesystem.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RideOrderServiceTest {

    private final RideOrderService service = new RideOrderService(null, null, null, null);

    @Test
    void calculatesBaseFeeForShortRide() {
        LocalDateTime start = LocalDateTime.of(2026, 10, 18, 8, 0);
        BigDecimal fee = service.calculateFee(start, start.plusMinutes(10), "合规");
        assertEquals(new BigDecimal("1.50"), fee);
    }

    @Test
    void addsExtraBlockAfterFifteenMinutes() {
        LocalDateTime start = LocalDateTime.of(2026, 10, 18, 8, 0);
        BigDecimal fee = service.calculateFee(start, start.plusMinutes(16), "合规");
        assertEquals(new BigDecimal("2.00"), fee);
    }

    @Test
    void addsViolationSurcharge() {
        LocalDateTime start = LocalDateTime.of(2026, 10, 18, 8, 0);
        BigDecimal fee = service.calculateFee(start, start.plusMinutes(8), "违规");
        assertEquals(new BigDecimal("3.00"), fee);
    }
}
