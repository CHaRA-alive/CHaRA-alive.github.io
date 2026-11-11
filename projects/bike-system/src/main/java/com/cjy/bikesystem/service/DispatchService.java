package com.cjy.bikesystem.service;

import com.cjy.bikesystem.entity.Bike;
import com.cjy.bikesystem.entity.DispatchPlan;
import com.cjy.bikesystem.entity.RideOrder;
import com.cjy.bikesystem.exception.BusinessException;
import com.cjy.bikesystem.repository.BikeRepository;
import com.cjy.bikesystem.repository.DispatchPlanRepository;
import com.cjy.bikesystem.repository.RideOrderRepository;
import com.cjy.bikesystem.web.dto.DispatchItem;
import com.cjy.bikesystem.web.dto.MaintenanceRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DispatchService {

    private static final String VERSION = "QingQi-Dispatch-1.0";
    private static final int WINDOW_DAYS = 14;
    private static final DateTimeFormatter PLAN_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final List<String> STANDARD_AREAS =
            List.of("教学区", "宿舍区", "图书馆", "食堂区", "体育馆");

    private final RideOrderRepository rideOrderRepository;
    private final BikeRepository bikeRepository;
    private final DispatchPlanRepository dispatchPlanRepository;
    private final MaintenanceWorkService maintenanceWorkService;
    private final ObjectMapper objectMapper;

    public DispatchService(
            RideOrderRepository rideOrderRepository,
            BikeRepository bikeRepository,
            DispatchPlanRepository dispatchPlanRepository,
            MaintenanceWorkService maintenanceWorkService,
            ObjectMapper objectMapper
    ) {
        this.rideOrderRepository = rideOrderRepository;
        this.bikeRepository = bikeRepository;
        this.dispatchPlanRepository = dispatchPlanRepository;
        this.maintenanceWorkService = maintenanceWorkService;
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> overview() {
        LocalDateTime now = LocalDateTime.now();
        List<RideOrder> allOrders = rideOrderRepository.findAll().stream()
                .filter(order -> order.getStartTime() != null)
                .sorted(Comparator.comparing(RideOrder::getStartTime).reversed())
                .toList();

        List<RideOrder> recentOrders = allOrders.stream()
                .filter(order -> order.getStartTime().isAfter(now.minusDays(WINDOW_DAYS)))
                .toList();
        boolean staleFallback = recentOrders.isEmpty();
        List<RideOrder> sampleOrders = staleFallback
                ? allOrders.stream().limit(60).toList()
                : recentOrders;

        LocalDateTime referenceTime = sampleOrders.isEmpty()
                ? now
                : sampleOrders.get(0).getStartTime();
        LocalDateTime windowStart = referenceTime.minusDays(WINDOW_DAYS - 1L);
        sampleOrders = sampleOrders.stream()
                .filter(order -> !order.getStartTime().isBefore(windowStart))
                .toList();

        List<Bike> bikes = bikeRepository.findAll();
        Map<String, Long> supply = bikes.stream()
                .filter(bike -> "可用".equals(bike.getBikeStatus()))
                .filter(bike -> !"损坏".equals(bike.getHealthStatus()))
                .collect(Collectors.groupingBy(
                        bike -> normalizeArea(bike.getArea()),
                        LinkedHashMap::new,
                        Collectors.counting()
                ));

        Map<String, Double> weightedDemand = new LinkedHashMap<>();
        STANDARD_AREAS.forEach(area -> weightedDemand.put(area, 0.0));
        for (RideOrder order : sampleOrders) {
            String area = normalizeArea(order.getStartPosition());
            long ageDays = Math.max(0, Duration.between(order.getStartTime(), referenceTime).toDays());
            double weight = Math.max(0.35, 1.0 - (ageDays / (double) WINDOW_DAYS));
            weightedDemand.merge(area, weight, Double::sum);
        }

        Set<String> allAreas = weightedDemand.keySet();
        long observedDays = sampleOrders.stream()
                .map(order -> order.getStartTime().toLocalDate())
                .distinct()
                .count();
        double effectiveDays = Math.max(1, Math.min(WINDOW_DAYS, observedDays));
        double totalWeight = weightedDemand.values().stream().mapToDouble(Double::doubleValue).sum();

        List<Map<String, Object>> zones = new ArrayList<>();
        for (String area : allAreas) {
            double demand = weightedDemand.getOrDefault(area, 0.0) / effectiveDays;
            long available = supply.getOrDefault(area, 0L);
            double gap = round(demand - available);
            double share = totalWeight == 0 ? 0 : weightedDemand.getOrDefault(area, 0.0) * 100 / totalWeight;
            String pressure;
            if (gap >= 2) {
                pressure = "高";
            } else if (gap >= 0.8) {
                pressure = "中";
            } else if (available - demand >= 2) {
                pressure = "富余";
            } else {
                pressure = "平稳";
            }

            Map<String, Object> zone = new LinkedHashMap<>();
            zone.put("area", area);
            zone.put("demandScore", round(demand));
            zone.put("demandShare", round(share));
            zone.put("availableBikes", available);
            zone.put("gap", gap);
            zone.put("pressure", pressure);
            zones.add(zone);
        }
        zones.sort(Comparator.comparingDouble(
                zone -> -((Number) zone.get("demandScore")).doubleValue()
        ));

        List<DispatchItem> items = buildItems(zones);
        double confidence = Math.min(97.0,
                52.0 + Math.min(28.0, sampleOrders.size() * 1.8) + Math.min(17.0, observedDays * 2.4));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("generatedAt", now);
        result.put("algorithmVersion", VERSION);
        result.put("sampleOrders", sampleOrders.size());
        result.put("observedDays", observedDays);
        result.put("windowDays", WINDOW_DAYS);
        result.put("sampleFrom", sampleOrders.isEmpty() ? null
                : sampleOrders.get(sampleOrders.size() - 1).getStartTime());
        result.put("sampleTo", sampleOrders.isEmpty() ? null : referenceTime);
        result.put("staleFallback", staleFallback);
        result.put("confidence", round(confidence));
        result.put("zones", zones);
        result.put("items", items);
        result.put("methodology", List.of(
                "按起点位置归一到校园功能区，统计近 " + WINDOW_DAYS + " 天真实骑行订单",
                "采用时间衰减加权：越接近当前时段的订单权重越高（最低 0.35）",
                "用加权日均需求减去区域可用车辆，形成供需缺口",
                "在富余区与缺口区之间匹配车辆，单次调配不超过 3 辆，降低一次性扰动",
                "执行方案时生成真实调度工单并更新车辆所属区域"
        ));
        return result;
    }

    @Transactional
    public Map<String, Object> generatePlan() {
        Map<String, Object> snapshot = overview();
        @SuppressWarnings("unchecked")
        List<DispatchItem> items = (List<DispatchItem>) snapshot.get("items");
        if (items.isEmpty()) {
            throw new BusinessException("当前区域供需基本平衡，无需生成调度方案");
        }

        DispatchItem first = items.get(0);
        String summary = "建议从" + first.fromArea() + "向" + first.toArea() + "调配"
                + first.bikeCount() + "辆，优先缓解高需求区域缺口。";
        DispatchPlan plan = new DispatchPlan();
        plan.setPlanId("DP" + LocalDateTime.now().format(PLAN_TIME));
        plan.setPlanDate(LocalDateTime.now());
        plan.setAlgorithmVersion(VERSION);
        plan.setConfidence(BigDecimal.valueOf(((Number) snapshot.get("confidence")).doubleValue()));
        plan.setSummary(summary);
        plan.setItemsJson(writeItems(items));
        plan.setStatus("待执行");
        plan.setCreatedAt(LocalDateTime.now());
        dispatchPlanRepository.save(plan);

        Map<String, Object> result = planView(plan);
        result.put("snapshot", snapshot);
        return result;
    }

    @Transactional
    public Map<String, Object> applyPlan(String planId) {
        DispatchPlan plan = dispatchPlanRepository.findById(planId)
                .orElseThrow(() -> new BusinessException("调度方案不存在"));
        if ("已执行".equals(plan.getStatus())) {
            return planView(plan);
        }

        int moved = 0;
        for (DispatchItem item : readItems(plan.getItemsJson())) {
            List<Bike> candidates = bikeRepository
                    .findByAreaAndBikeStatusOrderByBikeIdAsc(normalizeArea(item.fromArea()), "可用")
                    .stream()
                    .filter(bike -> !"损坏".equals(bike.getHealthStatus()))
                    .limit(item.bikeCount())
                    .toList();
            for (Bike bike : candidates) {
                String description = "智能调度 " + planId + "：由" + item.fromArea()
                        + "调往" + item.toArea() + "，需求缺口 " + item.demandGap();
                maintenanceWorkService.createWorkOrder(new MaintenanceRequest(
                        bike.getBikeId(),
                        "SYSTEM",
                        description,
                        null,
                        "调度"
                ));
                moved++;
            }
        }

        plan.setStatus(moved > 0 ? "已执行" : "执行失败");
        plan.setAppliedAt(LocalDateTime.now());
        dispatchPlanRepository.save(plan);
        Map<String, Object> result = planView(plan);
        result.put("movedBikes", moved);
        return result;
    }

    public List<Map<String, Object>> listPlans() {
        return dispatchPlanRepository.findAllByOrderByCreatedAtDesc().stream()
                .limit(20)
                .map(this::planView)
                .toList();
    }

    private List<DispatchItem> buildItems(List<Map<String, Object>> zones) {
        List<Map<String, Object>> deficits = zones.stream()
                .filter(zone -> ((Number) zone.get("gap")).doubleValue() > 0)
                .sorted(Comparator.comparingDouble(
                        zone -> -((Number) zone.get("gap")).doubleValue()))
                .toList();
        List<Map<String, Object>> surpluses = zones.stream()
                .filter(zone -> ((Number) zone.get("availableBikes")).doubleValue() > 0)
                .filter(zone -> ((Number) zone.get("availableBikes")).doubleValue()
                        - ((Number) zone.get("demandScore")).doubleValue() >= -0.5)
                .sorted(Comparator.comparingDouble(zone -> -(
                        ((Number) zone.get("availableBikes")).doubleValue()
                                - ((Number) zone.get("demandScore")).doubleValue())))
                .toList();

        List<DispatchItem> items = new ArrayList<>();
        Map<String, Long> remainingSupply = new LinkedHashMap<>();
        for (Map<String, Object> surplus : surpluses) {
            remainingSupply.put((String) surplus.get("area"),
                    ((Number) surplus.get("availableBikes")).longValue());
        }

        for (Map<String, Object> deficit : deficits) {
            double gap = ((Number) deficit.get("gap")).doubleValue();
            int needed = Math.max(1, Math.min(3, (int) Math.ceil(gap * 0.7)));
            for (Map<String, Object> surplus : surpluses) {
                if (needed <= 0) {
                    break;
                }
                String source = (String) surplus.get("area");
                long available = remainingSupply.getOrDefault(source, 0L);
                if (available <= 0) {
                    continue;
                }
                int count = (int) Math.min(needed, Math.min(3, available));
                if (count <= 0) {
                    continue;
                }
                String target = (String) deficit.get("area");
                items.add(new DispatchItem(
                        source,
                        target,
                        count,
                        ((Number) deficit.get("demandScore")).doubleValue(),
                        ((Number) deficit.get("availableBikes")).longValue(),
                        gap,
                        gap >= 2 ? "高" : "中",
                        target + "加权日均需求 " + deficit.get("demandScore")
                                + " 辆，当前可用 " + deficit.get("availableBikes")
                                + " 辆，预计缺口 " + gap + " 辆。"
                ));
                remainingSupply.put(source, available - count);
                needed -= count;
            }
        }
        return items;
    }

    private Map<String, Object> planView(DispatchPlan plan) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("planId", plan.getPlanId());
        result.put("planDate", plan.getPlanDate());
        result.put("algorithmVersion", plan.getAlgorithmVersion());
        result.put("confidence", plan.getConfidence());
        result.put("summary", plan.getSummary());
        result.put("items", readItems(plan.getItemsJson()));
        result.put("status", plan.getStatus());
        result.put("createdAt", plan.getCreatedAt());
        result.put("appliedAt", plan.getAppliedAt());
        return result;
    }

    public String normalizeArea(String text) {
        if (text == null || text.isBlank()) {
            return "其他";
        }
        if (text.contains("教学")) {
            return "教学区";
        }
        if (text.contains("宿舍")) {
            return "宿舍区";
        }
        if (text.contains("图书")) {
            return "图书馆";
        }
        if (text.contains("食堂") || text.contains("餐")) {
            return "食堂区";
        }
        if (text.contains("体育")) {
            return "体育馆";
        }
        return text.length() > 20 ? text.substring(0, 20) : text;
    }

    private String writeItems(List<DispatchItem> items) {
        try {
            return objectMapper.writeValueAsString(items);
        } catch (JsonProcessingException exception) {
            throw new BusinessException("调度方案序列化失败");
        }
    }

    private List<DispatchItem> readItems(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (JsonProcessingException exception) {
            throw new BusinessException("调度方案数据损坏");
        }
    }

    private double round(double value) {
        return BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }
}
