package com.cjy.bikesystem.service;

import com.cjy.bikesystem.entity.Bike;
import com.cjy.bikesystem.entity.BillRecord;
import com.cjy.bikesystem.entity.MaintenanceWork;
import com.cjy.bikesystem.entity.RideOrder;
import com.cjy.bikesystem.entity.SecurityRecord;
import com.cjy.bikesystem.entity.User;
import com.cjy.bikesystem.exception.BusinessException;
import com.cjy.bikesystem.repository.BillRecordRepository;
import com.cjy.bikesystem.repository.BikeRepository;
import com.cjy.bikesystem.repository.MaintenanceWorkRepository;
import com.cjy.bikesystem.repository.RideOrderRepository;
import com.cjy.bikesystem.repository.SecurityRecordRepository;
import com.cjy.bikesystem.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("MM-dd");

    private final UserRepository userRepository;
    private final BikeRepository bikeRepository;
    private final RideOrderRepository rideOrderRepository;
    private final MaintenanceWorkRepository maintenanceWorkRepository;
    private final SecurityRecordRepository securityRecordRepository;
    private final BillRecordRepository billRecordRepository;

    public AnalyticsService(
            UserRepository userRepository,
            BikeRepository bikeRepository,
            RideOrderRepository rideOrderRepository,
            MaintenanceWorkRepository maintenanceWorkRepository,
            SecurityRecordRepository securityRecordRepository,
            BillRecordRepository billRecordRepository
    ) {
        this.userRepository = userRepository;
        this.bikeRepository = bikeRepository;
        this.rideOrderRepository = rideOrderRepository;
        this.maintenanceWorkRepository = maintenanceWorkRepository;
        this.securityRecordRepository = securityRecordRepository;
        this.billRecordRepository = billRecordRepository;
    }

    public Map<String, Object> overview() {
        List<User> users = userRepository.findAll();
        List<Bike> bikes = bikeRepository.findAll();
        List<RideOrder> orders = sortedOrders();
        List<MaintenanceWork> works = maintenanceWorkRepository.findAll();
        List<SecurityRecord> violations = securityRecordRepository.findAll();
        LocalDate today = LocalDate.now();

        List<RideOrder> todayOrders = orders.stream()
                .filter(order -> order.getStartTime() != null)
                .filter(order -> order.getStartTime().toLocalDate().equals(today))
                .toList();
        BigDecimal todayRevenue = orders.stream()
                .filter(order -> "已支付".equals(order.getPayStatus()))
                .filter(order -> order.getEndTime() != null)
                .filter(order -> order.getEndTime().toLocalDate().equals(today))
                .map(RideOrder::getFee)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Long> bikeStatus = new LinkedHashMap<>();
        for (String status : List.of("可用", "使用中", "故障", "调度中")) {
            bikeStatus.put(status, bikes.stream()
                    .filter(bike -> status.equals(bike.getBikeStatus()))
                    .count());
        }

        List<Map<String, Object>> trend = new ArrayList<>();
        for (int offset = 6; offset >= 0; offset--) {
            LocalDate date = today.minusDays(offset);
            List<RideOrder> dayOrders = orders.stream()
                    .filter(order -> order.getStartTime() != null)
                    .filter(order -> order.getStartTime().toLocalDate().equals(date))
                    .toList();
            BigDecimal revenue = dayOrders.stream()
                    .filter(order -> "已支付".equals(order.getPayStatus()))
                    .map(RideOrder::getFee)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            trend.add(Map.of(
                    "date", date.toString(),
                    "label", date.format(DAY),
                    "orders", dayOrders.size(),
                    "revenue", revenue
            ));
        }

        Map<String, Object> kpis = new LinkedHashMap<>();
        kpis.put("totalUsers", users.size());
        kpis.put("activeUsers", users.stream().filter(user -> !"冻结".equals(user.getStatus())).count());
        kpis.put("totalBikes", bikes.size());
        kpis.put("availableBikes", bikes.stream().filter(bike -> "可用".equals(bike.getBikeStatus())).count());
        kpis.put("inUseBikes", bikes.stream().filter(bike -> "使用中".equals(bike.getBikeStatus())).count());
        kpis.put("todayOrders", todayOrders.size());
        kpis.put("todayRevenue", todayRevenue);
        kpis.put("pendingWorks", works.stream()
                .filter(work -> !"已完成".equals(work.getHandleStatus()))
                .count());
        kpis.put("unhandledViolations", violations.stream()
                .filter(record -> "未处理".equals(record.getHandleStatus()))
                .count());
        kpis.put("unpaidOrders", orders.stream()
                .filter(order -> "未支付".equals(order.getPayStatus()))
                .count());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("generatedAt", LocalDateTime.now());
        response.put("kpis", kpis);
        response.put("bikeStatus", bikeStatus.entrySet().stream()
                .map(entry -> Map.of("name", entry.getKey(), "value", entry.getValue()))
                .toList());
        response.put("orderTrend", trend);
        response.put("recentOrders", orders.stream().limit(8).map(this::orderView).toList());
        response.put("recentWorks", works.stream()
                .sorted(Comparator.comparing(MaintenanceWork::getReportTime,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(6)
                .map(this::workView)
                .toList());
        response.put("recentViolations", violations.stream()
                .sorted(Comparator.comparing(SecurityRecord::getReportTime,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(6)
                .map(this::violationView)
                .toList());
        return response;
    }

    public Map<String, Object> userHome(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        List<RideOrder> orders = rideOrderRepository.findByUser_UserId(userId).stream()
                .sorted(Comparator.comparing(RideOrder::getStartTime,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
        List<BillRecord> bills = billRecordRepository.findByUser(user).stream()
                .sorted(Comparator.comparing(BillRecord::getPayTime,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        BigDecimal totalSpent = orders.stream()
                .filter(order -> "已支付".equals(order.getPayStatus()))
                .map(RideOrder::getFee)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long unpaidCount = orders.stream()
                .filter(order -> "未支付".equals(order.getPayStatus()))
                .count();
        RideOrder activeOrder = orders.stream()
                .filter(order -> order.getEndTime() == null)
                .findFirst()
                .orElse(null);

        List<Map<String, Object>> creditEvents = new ArrayList<>();
        for (RideOrder order : orders) {
            if ("违规".equals(order.getParkStatus())) {
                creditEvents.add(Map.of(
                        "time", order.getEndTime() == null ? order.getStartTime() : order.getEndTime(),
                        "type", "违规停车",
                        "orderId", order.getOrderId(),
                        "location", order.getEndPosition() == null ? "未知" : order.getEndPosition(),
                        "impact", "信用标记"
                ));
            }
        }

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalRides", orders.size());
        stats.put("completedRides", orders.stream().filter(order -> order.getEndTime() != null).count());
        stats.put("unpaidCount", unpaidCount);
        stats.put("totalSpent", totalSpent);
        stats.put("availableBikeCount", bikeRepository.findByBikeStatus("可用").size());
        stats.put("hasActiveRide", activeOrder != null);
        stats.put("activeOrderId", activeOrder == null ? null : activeOrder.getOrderId());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("user", user);
        response.put("stats", stats);
        response.put("activeOrder", activeOrder == null ? null : orderView(activeOrder));
        response.put("recentOrders", orders.stream().limit(10).map(this::orderView).toList());
        response.put("bills", bills.stream().limit(10).map(this::billView).toList());
        response.put("creditEvents", creditEvents);
        return response;
    }

    public Map<String, Object> orderView(RideOrder order) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("orderId", order.getOrderId());
        value.put("userId", order.getUserId());
        value.put("bikeId", order.getBikeId());
        value.put("fee", order.getFee());
        value.put("parkStatus", order.getParkStatus());
        value.put("startTime", order.getStartTime());
        value.put("endTime", order.getEndTime());
        value.put("startPosition", order.getStartPosition());
        value.put("endPosition", order.getEndPosition());
        value.put("payStatus", order.getPayStatus());
        return value;
    }

    private Map<String, Object> workView(MaintenanceWork work) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("workId", work.getWorkId());
        value.put("bikeId", work.getBikeId());
        value.put("reporterId", work.getReporterId());
        value.put("desc", work.getDesc());
        value.put("reportTime", work.getReportTime());
        value.put("handleTime", work.getHandleTime());
        value.put("handlerId", work.getHandlerId());
        value.put("handleStatus", work.getHandleStatus());
        value.put("result", work.getResult());
        value.put("workType", work.getWorkType());
        return value;
    }

    private Map<String, Object> violationView(SecurityRecord record) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("reportId", record.getReportId());
        value.put("violationBikeId", record.getViolationBikeId());
        value.put("violationType", record.getViolationType());
        value.put("reporterId", record.getReporterId());
        value.put("violationPosition", record.getViolationPosition());
        value.put("reportTime", record.getReportTime());
        value.put("handleStatus", record.getHandleStatus());
        return value;
    }

    private Map<String, Object> billView(BillRecord bill) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("billId", bill.getBillId());
        value.put("orderId", bill.getRideOrder() == null ? null : bill.getRideOrder().getOrderId());
        value.put("amount", bill.getAmount());
        value.put("payMethod", bill.getPayMethod());
        value.put("payTime", bill.getPayTime());
        value.put("reason", bill.getReason());
        value.put("credit", bill.getCredit());
        return value;
    }

    private List<RideOrder> sortedOrders() {
        return rideOrderRepository.findAll().stream()
                .sorted(Comparator.comparing(RideOrder::getStartTime,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }
}
