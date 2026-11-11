package com.cjy.bikesystem.controller;

import com.cjy.bikesystem.entity.RideOrder;
import com.cjy.bikesystem.service.RideOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/ride")
public class RideController {

    @Autowired
    private RideOrderService rideOrderService;

    // 1. 开始骑行（扫码用车）
    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startRide(
            @RequestParam String userId,
            @RequestParam String bikeId,
            @RequestParam String startPosition) {

        Map<String, Object> result = new HashMap<>();

        try {
            // 创建新订单对象
            RideOrder order = new RideOrder();

            // 生成订单ID：O + 时间戳
            String orderId = "O" + System.currentTimeMillis();
            order.setOrderId(orderId);

            // 这里需要设置关联的用户和车辆
            // 注意：由于你的RideOrder使用@ManyToOne，这里需要完整的User和Bike对象
            // 简化版：先只设置基本字段

            order.setStartPosition(startPosition);
            order.setStartTime(java.time.LocalDateTime.now());
            order.setPayStatus("未支付");
            order.setParkStatus("合规");

            // 调用你现有的 createOrder 方法
            RideOrder createdOrder = rideOrderService.createOrder(order);

            if (createdOrder != null) {
                result.put("success", true);
                result.put("message", "骑行开始");
                result.put("orderId", createdOrder.getOrderId());
            } else {
                result.put("success", false);
                result.put("message", "创建订单失败");
            }

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "开始骑行失败: " + e.getMessage());
        }

        return ResponseEntity.ok(result);
    }

    // 2. 结束骑行
    @PostMapping("/end")
    public ResponseEntity<Map<String, Object>> endRide(
            @RequestParam String orderId,
            @RequestParam String endPosition) {

        Map<String, Object> result = new HashMap<>();

        try {
            // 调用你现有的 endOrder 方法
            RideOrder endedOrder = rideOrderService.endOrder(orderId, endPosition, "合规");

            if (endedOrder != null) {
                // 计算费用（简化版：固定费用）
                double fee = 1.50;

                result.put("success", true);
                result.put("message", "骑行结束");
                result.put("fee", fee);
                result.put("endTime", endedOrder.getEndTime());
            } else {
                result.put("success", false);
                result.put("message", "结束骑行失败，订单不存在");
            }

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "结束骑行失败: " + e.getMessage());
        }

        return ResponseEntity.ok(result);
    }

    // 3. 检查是否有进行中的骑行
    @GetMapping("/user/{userId}/active")
    public ResponseEntity<Map<String, Object>> checkActiveRide(@PathVariable String userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 获取用户的所有订单
            List<RideOrder> userOrders = rideOrderService.getOrdersByUserId(userId);

            // 查找未结束的订单（没有结束时间）
            RideOrder activeOrder = null;
            for (RideOrder order : userOrders) {
                if (order.getEndTime() == null) {
                    activeOrder = order;
                    break;
                }
            }

            result.put("hasActiveRide", activeOrder != null);
            result.put("success", true);

            if (activeOrder != null) {
                Map<String, Object> orderInfo = new HashMap<>();
                orderInfo.put("orderId", activeOrder.getOrderId());
                if (activeOrder.getBike() != null) {
                    orderInfo.put("bikeId", activeOrder.getBike().getBikeId());
                }
                orderInfo.put("startTime", activeOrder.getStartTime());
                orderInfo.put("startPosition", activeOrder.getStartPosition());
                result.put("activeOrder", orderInfo);
            }

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }

        return ResponseEntity.ok(result);
    }

    // 4. 计算费用（简化版）
    @GetMapping("/calculate-fee")
    public ResponseEntity<Map<String, Object>> calculateFee(@RequestParam String orderId) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 获取订单
            List<RideOrder> allOrders = rideOrderService.getAllOrders();
            RideOrder targetOrder = null;

            for (RideOrder order : allOrders) {
                if (orderId.equals(order.getOrderId())) {
                    targetOrder = order;
                    break;
                }
            }

            if (targetOrder != null) {
                // 简化费用计算：1.5元基础费
                double fee = 1.50;

                // 如果有违规停车，加收1.5元
                if ("违规".equals(targetOrder.getParkStatus())) {
                    fee += 1.50;
                }

                result.put("success", true);
                result.put("orderId", orderId);
                result.put("fee", fee);
            } else {
                result.put("success", false);
                result.put("message", "订单不存在");
            }

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }

        return ResponseEntity.ok(result);
    }

    // 5. 获取用户骑行统计
    @GetMapping("/user/{userId}/stats")
    public ResponseEntity<Map<String, Object>> getUserStats(@PathVariable String userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            List<RideOrder> userOrders = rideOrderService.getOrdersByUserId(userId);

            int totalRides = userOrders.size();
            int unpaidCount = 0;
            int completedRides = 0;

            for (RideOrder order : userOrders) {
                if ("未支付".equals(order.getPayStatus())) {
                    unpaidCount++;
                }
                if (order.getEndTime() != null) {
                    completedRides++;
                }
            }

            result.put("success", true);
            result.put("totalRides", totalRides);
            result.put("unpaidCount", unpaidCount);
            result.put("completedRides", completedRides);

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }

        return ResponseEntity.ok(result);
    }
}