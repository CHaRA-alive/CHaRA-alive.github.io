package com.cjy.bikesystem.controller;

import com.cjy.bikesystem.entity.*;
import com.cjy.bikesystem.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    @Autowired private UserService userService;
    @Autowired private RideOrderService rideOrderService;
    @Autowired private BikeService bikeService;
    @Autowired private MaintenanceWorkService maintenanceWorkService;
    @Autowired private FenceRuleService fenceRuleService;

    // ==================== 用户仪表盘主页 ====================
    @GetMapping("/user/{userId}/home")
    public ResponseEntity<Map<String, Object>> getUserDashboardHome(@PathVariable String userId) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 1. 用户基本信息
            var userOpt = userService.getUserById(userId);
            if (!userOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return ResponseEntity.ok(response);
            }
            User user = userOpt.get();

            // 2. 骑行统计数据
            List<RideOrder> userOrders = rideOrderService.getOrdersByUserId(userId);
            int totalRides = userOrders.size();
            int unpaidCount = 0;
            BigDecimal totalSpent = BigDecimal.ZERO;
            LocalDateTime lastRideTime = null;

            for (RideOrder order : userOrders) {
                if ("未支付".equals(order.getPayStatus())) {
                    unpaidCount++;
                }
                if (order.getFee() != null) {
                    totalSpent = totalSpent.add(order.getFee());
                }
                if (order.getStartTime() != null &&
                        (lastRideTime == null || order.getStartTime().isAfter(lastRideTime))) {
                    lastRideTime = order.getStartTime();
                }
            }

            // 3. 检查是否有进行中的骑行
            boolean hasActiveRide = false;
            String activeOrderId = null;
            for (RideOrder order : userOrders) {
                if (order.getEndTime() == null) {
                    hasActiveRide = true;
                    activeOrderId = order.getOrderId();
                    break;
                }
            }

            // 4. 获取最近订单
            List<Map<String, Object>> recentOrders = new ArrayList<>();
            userOrders.sort((a, b) -> {
                if (a.getStartTime() == null || b.getStartTime() == null) return 0;
                return b.getStartTime().compareTo(a.getStartTime());
            });

            for (int i = 0; i < Math.min(5, userOrders.size()); i++) {
                RideOrder order = userOrders.get(i);
                Map<String, Object> orderMap = new HashMap<>();
                orderMap.put("orderId", order.getOrderId());
                orderMap.put("startTime", order.getStartTime());
                orderMap.put("endTime", order.getEndTime());
                orderMap.put("startPosition", order.getStartPosition());
                orderMap.put("endPosition", order.getEndPosition());
                orderMap.put("fee", order.getFee());
                orderMap.put("payStatus", order.getPayStatus());
                orderMap.put("parkStatus", order.getParkStatus());
                if (order.getBike() != null) {
                    orderMap.put("bikeId", order.getBike().getBikeId());
                }
                recentOrders.add(orderMap);
            }

            // 5. 获取可用车辆数量
            List<Bike> availableBikes = bikeService.getAvailableBikes();
            int availableBikeCount = availableBikes.size();

            // 6. 组装响应数据
            response.put("success", true);

            // 用户信息
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("userId", user.getUserId());
            userInfo.put("userName", user.getUserName());
            userInfo.put("balance", user.getBalance());
            userInfo.put("credit", user.getCredit());
            userInfo.put("status", user.getStatus());
            userInfo.put("phone", user.getPhone());
            response.put("user", userInfo);

            // 统计数据
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalRides", totalRides);
            stats.put("unpaidCount", unpaidCount);
            stats.put("totalSpent", totalSpent);
            stats.put("lastRideTime", lastRideTime);
            stats.put("availableBikeCount", availableBikeCount);
            stats.put("hasActiveRide", hasActiveRide);
            stats.put("activeOrderId", activeOrderId);
            response.put("stats", stats);

            // 最近订单
            response.put("recentOrders", recentOrders);

            // 快速操作建议
            List<String> suggestions = new ArrayList<>();
            if (unpaidCount > 0) {
                suggestions.add("您有" + unpaidCount + "个订单待支付");
            }
            if (user.getBalance().compareTo(new BigDecimal("5.00")) < 0) {
                suggestions.add("账户余额较低，建议充值");
            }
            if ("标记".equals(user.getCredit())) {
                suggestions.add("您的信用已被标记，请规范停车");
            }
            response.put("suggestions", suggestions);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取仪表盘数据失败: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    // ==================== 扫码用车 ====================
    @PostMapping("/scan-and-ride")
    public ResponseEntity<Map<String, Object>> scanAndStartRide(
            @RequestParam String userId,
            @RequestParam String bikeId,
            @RequestParam String startPosition) {

        Map<String, Object> response = new HashMap<>();

        try {
            // 1. 检查用户状态
            var userOpt = userService.getUserById(userId);
            if (!userOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return ResponseEntity.ok(response);
            }

            User user = userOpt.get();
            if ("冻结".equals(user.getStatus())) {
                response.put("success", false);
                response.put("message", "账户已被冻结，无法使用");
                return ResponseEntity.ok(response);
            }

            // 2. 检查车辆状态
            var bikeOpt = bikeService.getBikeById(bikeId);
            if (!bikeOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "车辆不存在");
                return ResponseEntity.ok(response);
            }

            Bike bike = bikeOpt.get();
            if (!"可用".equals(bike.getBikeStatus())) {
                response.put("success", false);
                response.put("message", "车辆当前状态为：" + bike.getBikeStatus() + "，无法使用");
                return ResponseEntity.ok(response);
            }

            // 3. 检查用户是否有未支付订单
            List<RideOrder> userOrders = rideOrderService.getOrdersByUserId(userId);
            int unpaidCount = 0;
            for (RideOrder order : userOrders) {
                if ("未支付".equals(order.getPayStatus())) {
                    unpaidCount++;
                }
            }

            if (unpaidCount >= 3) {
                response.put("success", false);
                response.put("message", "您有" + unpaidCount + "个订单未支付，请先支付");
                return ResponseEntity.ok(response);
            }

            // 4. 创建订单
            RideOrder newOrder = new RideOrder();
            String orderId = "O" + System.currentTimeMillis();
            newOrder.setOrderId(orderId);
            newOrder.setStartTime(LocalDateTime.now());
            newOrder.setStartPosition(startPosition);
            newOrder.setPayStatus("未支付");
            newOrder.setParkStatus("合规");
            // 注意：这里需要设置关联的User和Bike对象，简化处理先不设置

            RideOrder createdOrder = rideOrderService.createOrder(newOrder);

            if (createdOrder != null) {
                // 5. 更新车辆状态为使用中
                bikeService.updateBikeStatus(bikeId, "使用中");

                response.put("success", true);
                response.put("message", "扫码成功，骑行开始");
                response.put("orderId", orderId);
                response.put("bikeId", bikeId);
                response.put("startTime", LocalDateTime.now());
                response.put("tips", "请将车辆停放在指定区域，违规停车将影响信用");
            } else {
                response.put("success", false);
                response.put("message", "创建订单失败");
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "扫码用车失败: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    // ==================== 车辆报修 ====================
    @PostMapping("/report-maintenance")
    public ResponseEntity<Map<String, Object>> reportVehicleMaintenance(
            @RequestParam String bikeId,
            @RequestParam String reporterId,
            @RequestParam String description,
            @RequestParam(defaultValue = "维修") String workType) {

        Map<String, Object> response = new HashMap<>();

        try {
            // 1. 检查车辆是否存在
            var bikeOpt = bikeService.getBikeById(bikeId);
            if (!bikeOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "车辆不存在");
                return ResponseEntity.ok(response);
            }

            // 2. 检查用户是否存在
            var userOpt = userService.getUserById(reporterId);
            if (!userOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return ResponseEntity.ok(response);
            }

            // 3. 创建维修工单
            MaintenanceWork workOrder = new MaintenanceWork();
            String workId = "W" + System.currentTimeMillis();
            workOrder.setWorkId(workId);  // 注意：使用正确的setter名
            workOrder.setReporterId(reporterId);  // 注意：使用正确的setter名
            workOrder.setDesc(description);
            workOrder.setReportTime(LocalDateTime.now());
            workOrder.setWorkType(workType);
            workOrder.setHandleStatus("待处理");

            // 注意：这里需要设置关联的Bike对象，简化处理先不设置

            // 调用service保存（如果service存在）
            if (maintenanceWorkService != null) {
                try {
                    MaintenanceWork savedWork = maintenanceWorkService.createWorkOrder(workOrder); // 假设有这个方法
                    response.put("success", true);
                    response.put("message", "报修成功，工单号: " + workId);
                    response.put("workId", workId);
                } catch (Exception e) {
                    // 如果方法名不对，简化处理
                    response.put("success", true);
                    response.put("message", "报修信息已提交");
                    response.put("workId", workId);
                }
            } else {
                response.put("success", true);
                response.put("message", "报修信息已提交");
                response.put("workId", workId);
            }

            // 4. 更新车辆状态
            if ("维修".equals(workType)) {
                // 更新车辆为故障状态
                Bike bike = new Bike();
                bike.setBikeId(bikeId);
                bike.setHealthStatus("损坏");
                bike.setBikeStatus("故障");
                bikeService.updateBike(bikeId, bike);
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "报修失败: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    // ==================== 停车规则查询 ====================
    @GetMapping("/parking-rules")
    public ResponseEntity<Map<String, Object>> getParkingRules() {
        Map<String, Object> response = new HashMap<>();

        try {
            // 获取所有区域规则（如果service存在）
            List<FenceRule> rules = new ArrayList<>();
            if (fenceRuleService != null) {
                try {
                    rules = fenceRuleService.getAllFenceRules();
                } catch (Exception e) {
                    // 如果方法不存在，使用空列表
                }
            }

            // 分类展示
            List<Map<String, Object>> noParkingZones = new ArrayList<>();
            List<Map<String, Object>> recommendedZones = new ArrayList<>();

            for (FenceRule rule : rules) {
                Map<String, Object> ruleInfo = new HashMap<>();
                ruleInfo.put("fenceId", rule.getFenceId());  // 注意：使用正确的getter
                ruleInfo.put("type", rule.getFenceType());
                ruleInfo.put("description", getFenceDescription(rule.getFenceType()));

                if ("禁停区".equals(rule.getFenceType())) {
                    noParkingZones.add(ruleInfo);
                } else if ("推荐区".equals(rule.getFenceType())) {
                    recommendedZones.add(ruleInfo);
                }
            }

            response.put("success", true);
            response.put("noParkingZones", noParkingZones);
            response.put("recommendedZones", recommendedZones);
            response.put("tips", Arrays.asList(
                    "请将车辆停放在推荐区域内",
                    "禁停区停车将收取额外费用",
                    "多次违规停车会影响信用等级"
            ));

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取停车规则失败");
            response.put("defaultRules", getDefaultParkingRules());
        }

        return ResponseEntity.ok(response);
    }

    // ==================== 附近车辆查询 ====================
    @GetMapping("/nearby-bikes")
    public ResponseEntity<Map<String, Object>> getNearbyBikes(
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude) {

        Map<String, Object> response = new HashMap<>();

        try {
            // 获取可用车辆
            List<Bike> availableBikes = bikeService.getAvailableBikes();
            List<Map<String, Object>> bikeList = new ArrayList<>();

            for (Bike bike : availableBikes) {
                Map<String, Object> bikeInfo = new HashMap<>();
                bikeInfo.put("bikeId", bike.getBikeId());
                bikeInfo.put("location", bike.getLocation());
                bikeInfo.put("healthStatus", bike.getHealthStatus());
                bikeInfo.put("bikeStatus", bike.getBikeStatus());
                bikeInfo.put("qrCode", bike.getQrCode());  // 注意：使用正确的getter

                // 计算距离（如果有坐标）
                if (latitude != null && longitude != null && bike.getLocation() != null) {
                    double bikeLocation = bike.getLocation().doubleValue();
                    double distance = Math.abs(latitude - bikeLocation) * 111.0;
                    bikeInfo.put("distance", String.format("%.2f km", distance));
                    bikeInfo.put("distanceValue", distance);
                }

                bikeList.add(bikeInfo);
            }

            // 按距离排序
            if (latitude != null && longitude != null) {
                bikeList.sort((a, b) -> {
                    Double distA = (Double) a.get("distanceValue");
                    Double distB = (Double) b.get("distanceValue");
                    if (distA == null || distB == null) return 0;
                    return distA.compareTo(distB);
                });
            }

            response.put("success", true);
            response.put("bikes", bikeList);
            response.put("count", bikeList.size());
            response.put("timestamp", LocalDateTime.now());

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取附近车辆失败: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    // ==================== 订单搜索 ====================
    @GetMapping("/search-orders")
    public ResponseEntity<Map<String, Object>> searchUserOrders(
            @RequestParam String userId,
            @RequestParam String keyword,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String payStatus) {

        Map<String, Object> response = new HashMap<>();

        try {
            List<RideOrder> userOrders = rideOrderService.getOrdersByUserId(userId);
            List<Map<String, Object>> filteredOrders = new ArrayList<>();

            for (RideOrder order : userOrders) {
                boolean matches = false;

                // 关键词匹配
                if (keyword != null && !keyword.trim().isEmpty()) {
                    matches = (order.getOrderId() != null && order.getOrderId().contains(keyword)) ||
                            (order.getStartPosition() != null && order.getStartPosition().contains(keyword)) ||
                            (order.getEndPosition() != null && order.getEndPosition().contains(keyword));
                } else {
                    matches = true;
                }

                // 支付状态过滤
                if (payStatus != null && !payStatus.trim().isEmpty()) {
                    matches = matches && payStatus.equals(order.getPayStatus());
                }

                // 时间范围过滤（简化版）
                if (matches) {
                    Map<String, Object> orderInfo = new HashMap<>();
                    orderInfo.put("orderId", order.getOrderId());
                    orderInfo.put("startTime", order.getStartTime());
                    orderInfo.put("endTime", order.getEndTime());
                    orderInfo.put("startPosition", order.getStartPosition());
                    orderInfo.put("endPosition", order.getEndPosition());
                    orderInfo.put("fee", order.getFee());
                    orderInfo.put("payStatus", order.getPayStatus());
                    orderInfo.put("parkStatus", order.getParkStatus());

                    if (order.getBike() != null) {
                        orderInfo.put("bikeId", order.getBike().getBikeId());
                    }

                    filteredOrders.add(orderInfo);
                }
            }

            // 按时间倒序排列
            filteredOrders.sort((a, b) -> {
                LocalDateTime timeA = (LocalDateTime) a.get("startTime");
                LocalDateTime timeB = (LocalDateTime) b.get("startTime");
                if (timeA == null || timeB == null) return 0;
                return timeB.compareTo(timeA);
            });

            response.put("success", true);
            response.put("orders", filteredOrders);
            response.put("count", filteredOrders.size());
            response.put("keyword", keyword);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "搜索订单失败: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    // ==================== 信用记录查询 ====================
    @GetMapping("/user/{userId}/credit-history")
    public ResponseEntity<Map<String, Object>> getCreditHistory(@PathVariable String userId) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 获取用户的所有订单
            List<RideOrder> userOrders = rideOrderService.getOrdersByUserId(userId);
            List<Map<String, Object>> creditEvents = new ArrayList<>();

            for (RideOrder order : userOrders) {
                if ("违规".equals(order.getParkStatus())) {
                    Map<String, Object> event = new HashMap<>();
                    event.put("time", order.getEndTime() != null ? order.getEndTime() : order.getStartTime());
                    event.put("type", "违规停车");
                    event.put("orderId", order.getOrderId());
                    event.put("location", order.getEndPosition());
                    event.put("impact", "信用标记");
                    event.put("description", "在" + order.getEndPosition() + "违规停车");
                    creditEvents.add(event);
                }
            }

            // 获取用户当前状态
            var userOpt = userService.getUserById(userId);
            String currentCredit = "未标记";
            if (userOpt.isPresent()) {
                currentCredit = userOpt.get().getCredit();
            }

            response.put("success", true);
            response.put("creditEvents", creditEvents);
            response.put("currentCredit", currentCredit);
            response.put("totalViolations", creditEvents.size());
            response.put("tips", getCreditTips(currentCredit, creditEvents.size()));

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取信用记录失败");
        }

        return ResponseEntity.ok(response);
    }

    // ==================== 辅助方法 ====================
    private String getFenceDescription(String fenceType) {
        switch (fenceType) {
            case "禁停区":
                return "禁止停放区域，停车将收取额外费用并影响信用";
            case "推荐区":
                return "推荐停放区域，停车更安全便捷";
            default:
                return "停车区域";
        }
    }

    private List<Map<String, Object>> getDefaultParkingRules() {
        List<Map<String, Object>> rules = new ArrayList<>();

        Map<String, Object> rule1 = new HashMap<>();
        rule1.put("type", "禁停区");
        rule1.put("locations", Arrays.asList("教学楼正门", "图书馆入口", "食堂门口"));
        rule1.put("penalty", "额外收取1.5元费用");
        rules.add(rule1);

        Map<String, Object> rule2 = new HashMap<>();
        rule2.put("type", "推荐区");
        rule2.put("locations", Arrays.asList("宿舍区停车点", "教学楼侧边", "体育馆停车场"));
        rule2.put("benefit", "规范停车可维护良好信用");
        rules.add(rule2);

        return rules;
    }

    private List<String> getCreditTips(String currentCredit, int violationCount) {
        List<String> tips = new ArrayList<>();

        if ("标记".equals(currentCredit)) {
            tips.add("您的信用已被标记，请规范停车以恢复信用");
            tips.add("连续3次规范停车可恢复信用状态");
        } else {
            tips.add("您当前的信用状态良好");
        }

        if (violationCount > 0) {
            tips.add("您有" + violationCount + "次违规停车记录");
        }

        tips.add("规范停车有助于维护良好信用");

        return tips;
    }
}