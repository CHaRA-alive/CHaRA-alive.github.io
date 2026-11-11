package com.cjy.bikesystem.service;

import com.cjy.bikesystem.entity.Bike;
import com.cjy.bikesystem.entity.BillRecord;
import com.cjy.bikesystem.entity.RideOrder;
import com.cjy.bikesystem.entity.User;
import com.cjy.bikesystem.exception.BusinessException;
import com.cjy.bikesystem.repository.BikeRepository;
import com.cjy.bikesystem.repository.BillRecordRepository;
import com.cjy.bikesystem.repository.RideOrderRepository;
import com.cjy.bikesystem.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class RideOrderService {

    private static final BigDecimal BASE_FEE = new BigDecimal("1.50");
    private static final BigDecimal EXTRA_FEE = new BigDecimal("0.50");
    private static final BigDecimal VIOLATION_FEE = new BigDecimal("1.50");
    private static final DateTimeFormatter ID_TIME = DateTimeFormatter.ofPattern("yyMMddHHmmss");

    private final RideOrderRepository rideOrderRepository;
    private final UserRepository userRepository;
    private final BikeRepository bikeRepository;
    private final BillRecordRepository billRecordRepository;

    public RideOrderService(
            RideOrderRepository rideOrderRepository,
            UserRepository userRepository,
            BikeRepository bikeRepository,
            BillRecordRepository billRecordRepository
    ) {
        this.rideOrderRepository = rideOrderRepository;
        this.userRepository = userRepository;
        this.bikeRepository = bikeRepository;
        this.billRecordRepository = billRecordRepository;
    }

    /**
     * 扫码开锁。用户、车辆与订单写入同一个事务，避免订单已建但车辆仍可租用的半完成状态。
     */
    @Transactional
    public RideOrder startRide(String userId, String bikeId, String startPosition) {
        User user = userRepository.findById(required(userId, "用户编号"))
                .orElseThrow(() -> new BusinessException("用户不存在"));
        if ("冻结".equals(user.getStatus())) {
            throw new BusinessException("账户已被冻结，暂时无法用车");
        }

        Bike bike = bikeRepository.findByBikeIdForUpdate(required(bikeId, "车辆编号"))
                .orElseThrow(() -> new BusinessException("车辆不存在"));
        if (!"可用".equals(bike.getBikeStatus())) {
            throw new BusinessException("车辆当前状态为“" + bike.getBikeStatus() + "”，无法开锁");
        }
        if ("损坏".equals(bike.getHealthStatus())) {
            throw new BusinessException("车辆健康状态异常，请选择其他车辆");
        }

        List<RideOrder> userOrders = rideOrderRepository.findByUser_UserId(userId);
        boolean hasActiveRide = userOrders.stream().anyMatch(order -> order.getEndTime() == null);
        if (hasActiveRide) {
            throw new BusinessException("您已有进行中的骑行，请先结束当前订单");
        }

        long unpaidCount = userOrders.stream()
                .filter(order -> "未支付".equals(order.getPayStatus()))
                .count();
        if (unpaidCount >= 3) {
            throw new BusinessException("未支付订单已达 3 笔，请先结清费用");
        }
        if (user.getBalance() == null || user.getBalance().compareTo(BASE_FEE) < 0) {
            throw new BusinessException("账户余额不足，请先充值后再用车");
        }

        RideOrder order = new RideOrder();
        order.setOrderId(nextId("O", rideOrderRepository::existsById));
        order.setUser(user);
        order.setBike(bike);
        order.setStartTime(LocalDateTime.now());
        order.setStartPosition(required(startPosition, "起始位置"));
        order.setPayStatus("未支付");
        order.setParkStatus("合规");
        RideOrder saved = rideOrderRepository.save(order);

        bike.setBikeStatus("使用中");
        bikeRepository.save(bike);
        return saved;
    }

    /**
     * 结束骑行并立即计算费用。停车位置和车辆状态在同一事务内更新。
     */
    @Transactional
    public RideOrder endOrder(String orderId, String endPosition, String parkStatus) {
        RideOrder order = rideOrderRepository.findById(required(orderId, "订单编号"))
                .orElseThrow(() -> new BusinessException("订单不存在"));
        if (order.getEndTime() != null) {
            throw new BusinessException("该订单已经结束");
        }
        if (order.getStartTime() == null) {
            throw new BusinessException("订单缺少开始时间，无法结算");
        }

        LocalDateTime endTime = LocalDateTime.now();
        if (endTime.isBefore(order.getStartTime())) {
            endTime = order.getStartTime();
        }
        String resolvedParkStatus = "违规".equals(parkStatus) ? "违规" : "合规";

        order.setEndTime(endTime);
        order.setEndPosition(required(endPosition, "结束位置"));
        order.setParkStatus(resolvedParkStatus);
        order.setFee(calculateFee(order.getStartTime(), endTime, resolvedParkStatus));

        Bike bike = order.getBike();
        if (bike != null) {
            bike.setBikeStatus("损坏".equals(bike.getHealthStatus()) ? "故障" : "可用");
            bikeRepository.save(bike);
        }

        if ("违规".equals(resolvedParkStatus) && order.getUser() != null) {
            User user = order.getUser();
            user.setCredit("标记");
            userRepository.save(user);
        }
        return rideOrderRepository.save(order);
    }

    /**
     * 余额支付订单，扣款与账单写入保持原子性。
     */
    @Transactional
    public BillRecord payOrder(String orderId, String payMethod) {
        RideOrder order = rideOrderRepository.findById(required(orderId, "订单编号"))
                .orElseThrow(() -> new BusinessException("订单不存在"));
        if (order.getEndTime() == null) {
            throw new BusinessException("骑行尚未结束，暂时不能支付");
        }
        if ("已支付".equals(order.getPayStatus())) {
            return billRecordRepository.findByRideOrder_OrderId(orderId).stream()
                    .findFirst()
                    .orElseThrow(() -> new BusinessException("订单已支付，但账单记录缺失"));
        }

        User user = order.getUser();
        BigDecimal amount = order.getFee() == null ? BigDecimal.ZERO : order.getFee();
        if (user == null) {
            throw new BusinessException("订单缺少用户信息");
        }
        if (user.getBalance() == null || user.getBalance().compareTo(amount) < 0) {
            BigDecimal balance = user.getBalance() == null ? BigDecimal.ZERO : user.getBalance();
            throw new BusinessException("账户余额不足，请先充值 "
                    + amount.subtract(balance).setScale(2, RoundingMode.HALF_UP) + " 元");
        }

        user.setBalance(user.getBalance().subtract(amount));
        userRepository.save(user);
        order.setPayStatus("已支付");
        rideOrderRepository.save(order);

        BillRecord bill = new BillRecord();
        bill.setBillId(nextId("B", billRecordRepository::existsById));
        bill.setRideOrder(order);
        bill.setUser(user);
        bill.setAmount(amount);
        bill.setPayMethod(payMethod == null || payMethod.isBlank() ? "余额" : payMethod.trim());
        bill.setPayTime(LocalDateTime.now());
        bill.setCredit(user.getCredit());
        bill.setReason("违规".equals(order.getParkStatus())
                ? "骑行费用及违规停车处理费"
                : "正常骑行费用");
        return billRecordRepository.save(bill);
    }

    public BigDecimal calculateFee(LocalDateTime startTime, LocalDateTime endTime, String parkStatus) {
        long minutes = Math.max(1, Duration.between(startTime, endTime).toMinutes());
        BigDecimal fee = BASE_FEE;
        if (minutes > 15) {
            long extraBlocks = (long) Math.ceil((minutes - 15) / 15.0);
            fee = fee.add(EXTRA_FEE.multiply(BigDecimal.valueOf(extraBlocks)));
        }
        if ("违规".equals(parkStatus)) {
            fee = fee.add(VIOLATION_FEE);
        }
        return fee.setScale(2, RoundingMode.HALF_UP);
    }

    public List<RideOrder> getOrdersByUserId(String userId) {
        return rideOrderRepository.findByUser_UserId(userId);
    }

    public List<RideOrder> getOrdersByBikeId(String bikeId) {
        return rideOrderRepository.findByBike_BikeId(bikeId);
    }

    public List<RideOrder> getOrdersByPayStatus(String payStatus) {
        return rideOrderRepository.findByPayStatus(payStatus);
    }

    public List<RideOrder> getOrdersByTimeRange(LocalDateTime start, LocalDateTime end) {
        return rideOrderRepository.findByStartTimeBetween(start, end);
    }

    public List<RideOrder> getAllOrders() {
        return rideOrderRepository.findAll();
    }

    public Optional<RideOrder> getOrderById(String orderId) {
        return rideOrderRepository.findById(orderId);
    }

    public Optional<RideOrder> getActiveOrder(String userId) {
        return rideOrderRepository.findByUser_UserId(userId).stream()
                .filter(order -> order.getEndTime() == null)
                .findFirst();
    }

    private String required(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(fieldName + "不能为空");
        }
        return value.trim();
    }

    private String nextId(String prefix, java.util.function.Predicate<String> exists) {
        String base = prefix + LocalDateTime.now().format(ID_TIME).substring(1);
        String candidate = base;
        int suffix = 0;
        while (exists.test(candidate)) {
            suffix++;
            String suffixText = String.valueOf(suffix);
            candidate = base.substring(0, 12 - suffixText.length()) + suffixText;
        }
        return candidate;
    }
}
