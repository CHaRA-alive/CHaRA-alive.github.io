package com.cjy.bikesystem.repository;

import com.cjy.bikesystem.entity.RideOrder;
import com.cjy.bikesystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RideOrderRepository extends JpaRepository<RideOrder, String> {

    // 根据用户查询订单
    List<RideOrder> findByUser(User user);

    // 根据用户ID查询订单
    List<RideOrder> findByUser_UserId(String userId);

    // 根据车辆查询订单
    List<RideOrder> findByBike_BikeId(String bikeId);

    // 根据支付状态查询
    List<RideOrder> findByPayStatus(String payStatus);

    // 根据停车合规状态查询
    List<RideOrder> findByParkStatus(String parkStatus);

    // 根据时间范围查询
    List<RideOrder> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);
}