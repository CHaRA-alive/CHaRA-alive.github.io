package com.cjy.bikesystem.controller;

import com.cjy.bikesystem.entity.BillRecord;
import com.cjy.bikesystem.entity.RideOrder;
import com.cjy.bikesystem.service.AnalyticsService;
import com.cjy.bikesystem.service.RideOrderService;
import com.cjy.bikesystem.web.dto.PaymentRequest;
import com.cjy.bikesystem.web.dto.StartRideRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class RideOrderController {

    private final RideOrderService rideOrderService;
    private final AnalyticsService analyticsService;

    public RideOrderController(RideOrderService rideOrderService, AnalyticsService analyticsService) {
        this.rideOrderService = rideOrderService;
        this.analyticsService = analyticsService;
    }

    @GetMapping
    public List<RideOrder> getAllOrders() {
        return rideOrderService.getAllOrders();
    }

    @PostMapping({"", "/start"})
    public Map<String, Object> startOrder(@RequestBody StartRideRequest request) {
        RideOrder order = rideOrderService.startRide(
                request.userId(), request.bikeId(), request.startPosition()
        );
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("order", analyticsService.orderView(order));
        return response;
    }

    @PutMapping("/{orderId}/end")
    public Map<String, Object> endOrder(
            @PathVariable String orderId,
            @RequestParam String endPosition,
            @RequestParam(defaultValue = "合规") String parkStatus
    ) {
        RideOrder order = rideOrderService.endOrder(orderId, endPosition, parkStatus);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("order", analyticsService.orderView(order));
        return response;
    }

    @RequestMapping(value = "/{orderId}/pay", method = {RequestMethod.POST, RequestMethod.PUT})
    public Map<String, Object> payOrder(
            @PathVariable String orderId,
            @RequestBody(required = false) PaymentRequest request
    ) {
        BillRecord bill = rideOrderService.payOrder(
                orderId,
                request == null ? "余额" : request.resolvedPayMethod()
        );
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("billId", bill.getBillId());
        response.put("amount", bill.getAmount());
        return response;
    }

    @GetMapping("/user/{userId}")
    public List<RideOrder> getOrdersByUserId(@PathVariable String userId) {
        return rideOrderService.getOrdersByUserId(userId);
    }

    @GetMapping("/user/{userId}/active")
    public Map<String, Object> getActiveOrder(@PathVariable String userId) {
        Map<String, Object> response = new LinkedHashMap<>();
        var active = rideOrderService.getActiveOrder(userId);
        response.put("hasActiveRide", active.isPresent());
        response.put("activeOrder", active.map(analyticsService::orderView).orElse(null));
        return response;
    }

    @GetMapping("/bike/{bikeId}")
    public List<RideOrder> getOrdersByBikeId(@PathVariable String bikeId) {
        return rideOrderService.getOrdersByBikeId(bikeId);
    }

    @GetMapping("/pay-status/{payStatus}")
    public List<RideOrder> getOrdersByPayStatus(@PathVariable String payStatus) {
        return rideOrderService.getOrdersByPayStatus(payStatus);
    }

    @GetMapping("/time-range")
    public List<RideOrder> getOrdersByTimeRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        return rideOrderService.getOrdersByTimeRange(start, end);
    }

    @GetMapping("/{orderId}")
    public RideOrder getOrderById(@PathVariable String orderId) {
        return rideOrderService.getOrderById(orderId)
                .orElseThrow(() -> new com.cjy.bikesystem.exception.BusinessException("订单不存在"));
    }

    @GetMapping("/{orderId}/fee")
    public Map<String, Object> calculateFee(@PathVariable String orderId) {
        RideOrder order = rideOrderService.getOrderById(orderId)
                .orElseThrow(() -> new com.cjy.bikesystem.exception.BusinessException("订单不存在"));
        BigDecimal fee = rideOrderService.calculateFee(
                order.getStartTime(),
                order.getEndTime() == null ? LocalDateTime.now() : order.getEndTime(),
                order.getParkStatus()
        );
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("orderId", orderId);
        response.put("fee", fee);
        return response;
    }
}
