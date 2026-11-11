package com.cjy.bikesystem.controller;

import com.cjy.bikesystem.entity.Bike;
import com.cjy.bikesystem.entity.FenceRule;
import com.cjy.bikesystem.entity.RideOrder;
import com.cjy.bikesystem.service.AnalyticsService;
import com.cjy.bikesystem.service.BikeService;
import com.cjy.bikesystem.service.FenceRuleService;
import com.cjy.bikesystem.service.MaintenanceWorkService;
import com.cjy.bikesystem.service.RideOrderService;
import com.cjy.bikesystem.web.dto.MaintenanceRequest;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final AnalyticsService analyticsService;
    private final RideOrderService rideOrderService;
    private final MaintenanceWorkService maintenanceWorkService;
    private final FenceRuleService fenceRuleService;
    private final BikeService bikeService;

    public DashboardController(
            AnalyticsService analyticsService,
            RideOrderService rideOrderService,
            MaintenanceWorkService maintenanceWorkService,
            FenceRuleService fenceRuleService,
            BikeService bikeService
    ) {
        this.analyticsService = analyticsService;
        this.rideOrderService = rideOrderService;
        this.maintenanceWorkService = maintenanceWorkService;
        this.fenceRuleService = fenceRuleService;
        this.bikeService = bikeService;
    }

    @GetMapping("/overview")
    public Map<String, Object> overview() {
        return analyticsService.overview();
    }

    @GetMapping("/user/{userId}/home")
    public Map<String, Object> getUserDashboardHome(@PathVariable String userId) {
        return analyticsService.userHome(userId);
    }

    @PostMapping("/scan-and-ride")
    public Map<String, Object> scanAndStartRide(
            @RequestParam String userId,
            @RequestParam String bikeId,
            @RequestParam String startPosition
    ) {
        RideOrder order = rideOrderService.startRide(userId, bikeId, startPosition);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "扫码成功，骑行开始");
        response.put("order", analyticsService.orderView(order));
        return response;
    }

    @PostMapping("/report-maintenance")
    public Map<String, Object> reportVehicleMaintenance(
            @RequestParam String bikeId,
            @RequestParam String reporterId,
            @RequestParam String description,
            @RequestParam(defaultValue = "维修") String workType
    ) {
        var work = maintenanceWorkService.createWorkOrder(
                new MaintenanceRequest(bikeId, reporterId, description, null, workType)
        );
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "报修成功");
        response.put("workId", work.getWorkId());
        return response;
    }

    @GetMapping("/parking-rules")
    public Map<String, Object> getParkingRules() {
        List<FenceRule> rules = fenceRuleService.getAllFenceRules();
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("noParkingZones", rules.stream()
                .filter(rule -> "禁停区".equals(rule.getFenceType()))
                .toList());
        response.put("recommendedZones", rules.stream()
                .filter(rule -> "推荐区".equals(rule.getFenceType()))
                .toList());
        response.put("tips", List.of(
                "请将车辆停放在推荐区域内",
                "禁停区停车将收取额外费用",
                "多次违规停车会影响信用等级"
        ));
        return response;
    }

    @GetMapping("/nearby-bikes")
    public Map<String, Object> getNearbyBikes() {
        List<Bike> bikes = bikeService.getAvailableBikes();
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("bikes", bikes);
        response.put("count", bikes.size());
        response.put("timestamp", LocalDateTime.now());
        return response;
    }

    @GetMapping("/search-orders")
    public Map<String, Object> searchUserOrders(
            @RequestParam String userId,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) String payStatus
    ) {
        String normalized = keyword.trim().toLowerCase();
        List<RideOrder> orders = rideOrderService.getOrdersByUserId(userId).stream()
                .filter(order -> normalized.isEmpty()
                        || contains(order.getOrderId(), normalized)
                        || contains(order.getStartPosition(), normalized)
                        || contains(order.getEndPosition(), normalized))
                .filter(order -> payStatus == null || payStatus.isBlank()
                        || payStatus.equals(order.getPayStatus()))
                .toList();
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("orders", orders.stream().map(analyticsService::orderView).toList());
        response.put("count", orders.size());
        return response;
    }

    @GetMapping("/user/{userId}/credit-history")
    public Map<String, Object> getCreditHistory(@PathVariable String userId) {
        Map<String, Object> home = analyticsService.userHome(userId);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("creditEvents", home.get("creditEvents"));
        response.put("currentCredit", ((com.cjy.bikesystem.entity.User) home.get("user")).getCredit());
        response.put("totalViolations", ((List<?>) home.get("creditEvents")).size());
        return response;
    }

    private boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase().contains(keyword);
    }
}
