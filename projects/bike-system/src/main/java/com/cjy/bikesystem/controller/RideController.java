package com.cjy.bikesystem.controller;

import com.cjy.bikesystem.entity.RideOrder;
import com.cjy.bikesystem.service.AnalyticsService;
import com.cjy.bikesystem.service.RideOrderService;
import com.cjy.bikesystem.web.dto.StartRideRequest;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ride")
public class RideController {

    private final RideOrderService rideOrderService;
    private final AnalyticsService analyticsService;

    public RideController(RideOrderService rideOrderService, AnalyticsService analyticsService) {
        this.rideOrderService = rideOrderService;
        this.analyticsService = analyticsService;
    }

    @PostMapping("/start")
    public Map<String, Object> startRide(@RequestBody StartRideRequest request) {
        RideOrder order = rideOrderService.startRide(
                request.userId(), request.bikeId(), request.startPosition()
        );
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "骑行开始");
        response.put("order", analyticsService.orderView(order));
        return response;
    }

    @PostMapping("/end")
    public Map<String, Object> endRide(
            @RequestParam String orderId,
            @RequestParam String endPosition,
            @RequestParam(defaultValue = "合规") String parkStatus
    ) {
        RideOrder order = rideOrderService.endOrder(orderId, endPosition, parkStatus);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "骑行结束");
        response.put("order", analyticsService.orderView(order));
        return response;
    }

    @GetMapping("/user/{userId}/active")
    public Map<String, Object> checkActiveRide(@PathVariable String userId) {
        Map<String, Object> response = new LinkedHashMap<>();
        var active = rideOrderService.getActiveOrder(userId);
        response.put("success", true);
        response.put("hasActiveRide", active.isPresent());
        response.put("activeOrder", active.map(analyticsService::orderView).orElse(null));
        return response;
    }

    @GetMapping("/user/{userId}/stats")
    public Map<String, Object> getUserStats(@PathVariable String userId) {
        var orders = rideOrderService.getOrdersByUserId(userId);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("totalRides", orders.size());
        response.put("completedRides", orders.stream().filter(o -> o.getEndTime() != null).count());
        response.put("unpaidCount", orders.stream()
                .filter(o -> "未支付".equals(o.getPayStatus())).count());
        return response;
    }
}
