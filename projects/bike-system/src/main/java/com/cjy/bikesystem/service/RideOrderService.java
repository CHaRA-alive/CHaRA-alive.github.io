package com.cjy.bikesystem.service;

import com.cjy.bikesystem.entity.RideOrder;
import com.cjy.bikesystem.entity.User;
import com.cjy.bikesystem.entity.Bike;
import com.cjy.bikesystem.repository.RideOrderRepository;
import com.cjy.bikesystem.repository.UserRepository;
import com.cjy.bikesystem.repository.BikeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class RideOrderService {

    @Autowired
    private RideOrderRepository rideOrderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BikeRepository bikeRepository;

    // 1. 创建骑行订单
    public RideOrder createOrder(RideOrder order) {
        // 验证用户和车辆是否存在
        Optional<User> user = userRepository.findById(order.getUser().getUserId());
        Bike bike = bikeRepository.findByBikeId(order.getBike().getBikeId());

        if (user.isPresent() && bike != null) {
            order.setUser(user.get());
            order.setBike(bike);
            order.setStartTime(LocalDateTime.now());
            order.setPayStatus("未支付");
            return rideOrderRepository.save(order);
        }
        return null;
    }

    // 2. 结束骑行
    public RideOrder endOrder(String orderId, String endPosition, String parkStatus) {
        Optional<RideOrder> optionalOrder = rideOrderRepository.findById(orderId);
        if (optionalOrder.isPresent()) {
            RideOrder order = optionalOrder.get();
            order.setEndTime(LocalDateTime.now());
            order.setEndPosition(endPosition);
            order.setParkStatus(parkStatus);

            // 计算费用（简单示例：按分钟计费）
            long minutes = java.time.Duration.between(order.getStartTime(), order.getEndTime()).toMinutes();
            BigDecimal fee = BigDecimal.valueOf(minutes * 0.5); // 每分钟0.5元
            order.setFee(fee);

            return rideOrderRepository.save(order);
        }
        return null;
    }

    // 3. 支付订单
    public boolean payOrder(String orderId) {
        Optional<RideOrder> optionalOrder = rideOrderRepository.findById(orderId);
        if (optionalOrder.isPresent()) {
            RideOrder order = optionalOrder.get();
            order.setPayStatus("已支付");
            rideOrderRepository.save(order);
            return true;
        }
        return false;
    }

    // 4. 获取用户的所有订单
    public List<RideOrder> getOrdersByUserId(String userId) {
        return rideOrderRepository.findByUser_UserId(userId);
    }

    // 5. 获取车辆的所有订单
    public List<RideOrder> getOrdersByBikeId(String bikeId) {
        return rideOrderRepository.findByBike_BikeId(bikeId);
    }

    // 6. 根据支付状态查询订单
    public List<RideOrder> getOrdersByPayStatus(String payStatus) {
        return rideOrderRepository.findByPayStatus(payStatus);
    }

    // 7. 根据时间范围查询订单
    public List<RideOrder> getOrdersByTimeRange(LocalDateTime start, LocalDateTime end) {
        return rideOrderRepository.findByStartTimeBetween(start, end);
    }

    // 8. 获取所有订单
    public List<RideOrder> getAllOrders() {
        return rideOrderRepository.findAll();
    }

}