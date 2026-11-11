package com.cjy.bikesystem.service;

import com.cjy.bikesystem.entity.Bike;
import com.cjy.bikesystem.repository.BikeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class BikeService {

    @Autowired
    private BikeRepository bikeRepository;

    // 1. 获取所有车辆
    public List<Bike> getAllBikes() {
        return bikeRepository.findAll();
    }

    // 2. 根据ID获取车辆
    public Optional<Bike> getBikeById(String bikeId) {
        return Optional.ofNullable(bikeRepository.findByBikeId(bikeId));
    }

    // 3. 添加车辆
    public Bike addBike(Bike bike) {
        return bikeRepository.save(bike);
    }

    // 4. 更新车辆
    public Bike updateBike(String bikeId, Bike bikeDetails) {
        Bike bike = bikeRepository.findByBikeId(bikeId);
        if (bike != null) {
            bike.setQrCode(bikeDetails.getQrCode());
            bike.setBikeStatus(bikeDetails.getBikeStatus());
            bike.setHealthStatus(bikeDetails.getHealthStatus());
            bike.setLocation(bikeDetails.getLocation());
            return bikeRepository.save(bike);
        }
        return null;
    }

    // 5. 删除车辆
    public boolean deleteBike(String bikeId) {
        if (bikeRepository.existsById(bikeId)) {
            bikeRepository.deleteById(bikeId);
            return true;
        }
        return false;
    }

    // 6. 根据状态查询车辆
    public List<Bike> getBikesByStatus(String status) {
        return bikeRepository.findByBikeStatus(status);
    }

    // 7. 根据健康状态查询车辆
    public List<Bike> getBikesByHealthStatus(String healthStatus) {
        return bikeRepository.findByHealthStatus(healthStatus);
    }

    // 8. 更新车辆状态
    public boolean updateBikeStatus(String bikeId, String status) {
        Bike bike = bikeRepository.findByBikeId(bikeId);
        if (bike != null) {
            bike.setBikeStatus(status);
            bikeRepository.save(bike);
            return true;
        }
        return false;
    }

    // 9. 更新车辆位置
    public boolean updateBikeLocation(String bikeId, BigDecimal location) {
        Bike bike = bikeRepository.findByBikeId(bikeId);
        if (bike != null) {
            bike.setLocation(location);
            bikeRepository.save(bike);
            return true;
        }
        return false;
    }

    // 10. 获取可用车辆列表
    public List<Bike> getAvailableBikes() {
        return bikeRepository.findByBikeStatus("可用");
    }
}