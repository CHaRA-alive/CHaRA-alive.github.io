package com.cjy.bikesystem.controller;

import com.cjy.bikesystem.entity.RideOrder;
import com.cjy.bikesystem.service.RideOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class RideOrderController {

    @Autowired
    private RideOrderService rideOrderService;

    // 1. 获取所有订单
    @GetMapping
    public List<RideOrder> getAllOrders() {
        return rideOrderService.getAllOrders();
    }

    // 2. 创建订单
    @PostMapping
    public ResponseEntity<RideOrder> createOrder(@RequestBody RideOrder order) {
        RideOrder createdOrder = rideOrderService.createOrder(order);
        return createdOrder != null ? ResponseEntity.ok(createdOrder) : ResponseEntity.badRequest().build();
    }

    // 3. 结束订单
    @PutMapping("/{orderId}/end")
    public ResponseEntity<RideOrder> endOrder(
            @PathVariable String orderId,
            @RequestParam String endPosition,
            @RequestParam String parkStatus) {
        RideOrder endedOrder = rideOrderService.endOrder(orderId, endPosition, parkStatus);
        return endedOrder != null ? ResponseEntity.ok(endedOrder) : ResponseEntity.notFound().build();
    }

    // 4. 支付订单
    @PutMapping("/{orderId}/pay")
    public ResponseEntity<Map<String, Boolean>> payOrder(@PathVariable String orderId) {
        boolean success = rideOrderService.payOrder(orderId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("success", success);
        return success ? ResponseEntity.ok(response) : ResponseEntity.notFound().build();
    }

    // 5. 根据用户查询订单
    @GetMapping("/user/{userId}")
    public List<RideOrder> getOrdersByUserId(@PathVariable String userId) {
        return rideOrderService.getOrdersByUserId(userId);
    }

    // 6. 根据车辆查询订单
    @GetMapping("/bike/{bikeId}")
    public List<RideOrder> getOrdersByBikeId(@PathVariable String bikeId) {
        return rideOrderService.getOrdersByBikeId(bikeId);
    }

    // 7. 根据支付状态查询订单
    @GetMapping("/pay-status/{payStatus}")
    public List<RideOrder> getOrdersByPayStatus(@PathVariable String payStatus) {
        return rideOrderService.getOrdersByPayStatus(payStatus);
    }

    // 8. 根据时间范围查询订单
    @GetMapping("/time-range")
    public List<RideOrder> getOrdersByTimeRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return rideOrderService.getOrdersByTimeRange(start, end);
    }

    // 9. 获取订单详情
    @GetMapping("/{orderId}")
    public ResponseEntity<RideOrder> getOrderById(@PathVariable String orderId) {
        var order = rideOrderService.getAllOrders().stream()
                .filter(o -> o.getOrderId().equals(orderId))
                .findFirst();
        return order.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}