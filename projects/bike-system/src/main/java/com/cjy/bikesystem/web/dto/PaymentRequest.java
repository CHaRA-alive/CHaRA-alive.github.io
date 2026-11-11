package com.cjy.bikesystem.web.dto;

public record PaymentRequest(String payMethod) {

    public String resolvedPayMethod() {
        return payMethod == null || payMethod.isBlank() ? "余额" : payMethod.trim();
    }
}
