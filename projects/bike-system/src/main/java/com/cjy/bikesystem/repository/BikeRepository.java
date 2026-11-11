package com.cjy.bikesystem.repository;

import com.cjy.bikesystem.entity.Bike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BikeRepository extends JpaRepository<Bike, String> {

    // 根据车辆状态查询
    List<Bike> findByBikeStatus(String bikeStatus);

    // 根据健康状态查询
    List<Bike> findByHealthStatus(String healthStatus);

    // 根据车辆编号查询
    Bike findByBikeId(String bikeId);
}