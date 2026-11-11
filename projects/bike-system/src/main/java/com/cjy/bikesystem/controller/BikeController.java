package com.cjy.bikesystem.controller;

import com.cjy.bikesystem.entity.Bike;
import com.cjy.bikesystem.service.BikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bikes")
public class BikeController {

    @Autowired
    private BikeService bikeService;

    // 1. 获取所有车辆
    @GetMapping
    public List<Bike> getAllBikes() {
        return bikeService.getAllBikes();
    }

    // 2. 根据ID获取车辆
    @GetMapping("/{bikeId}")
    public ResponseEntity<Bike> getBikeById(@PathVariable String bikeId) {
        var bike = bikeService.getBikeById(bikeId);
        return bike.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 3. 创建车辆
    @PostMapping
    public Bike createBike(@RequestBody Bike bike) {
        return bikeService.addBike(bike);
    }

    // 4. 更新车辆
    @PutMapping("/{bikeId}")
    public ResponseEntity<Bike> updateBike(@PathVariable String bikeId, @RequestBody Bike bikeDetails) {
        Bike updatedBike = bikeService.updateBike(bikeId, bikeDetails);
        return updatedBike != null ? ResponseEntity.ok(updatedBike) : ResponseEntity.notFound().build();
    }

    // 5. 删除车辆
    @DeleteMapping("/{bikeId}")
    public ResponseEntity<Map<String, Boolean>> deleteBike(@PathVariable String bikeId) {
        boolean deleted = bikeService.deleteBike(bikeId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("deleted", deleted);
        return deleted ? ResponseEntity.ok(response) : ResponseEntity.notFound().build();
    }

    // 6. 根据状态查询车辆
    @GetMapping("/status/{status}")
    public List<Bike> getBikesByStatus(@PathVariable String status) {
        return bikeService.getBikesByStatus(status);
    }

    // 7. 根据健康状态查询车辆
    @GetMapping("/health/{healthStatus}")
    public List<Bike> getBikesByHealthStatus(@PathVariable String healthStatus) {
        return bikeService.getBikesByHealthStatus(healthStatus);
    }

    // 8. 更新车辆状态
    @PutMapping("/{bikeId}/status")
    public ResponseEntity<Map<String, Boolean>> updateBikeStatus(
            @PathVariable String bikeId,
            @RequestParam String status) {
        boolean success = bikeService.updateBikeStatus(bikeId, status);
        Map<String, Boolean> response = new HashMap<>();
        response.put("success", success);
        return success ? ResponseEntity.ok(response) : ResponseEntity.notFound().build();
    }

    // 9. 更新车辆位置
    @PutMapping("/{bikeId}/location")
    public ResponseEntity<Map<String, Boolean>> updateBikeLocation(
            @PathVariable String bikeId,
            @RequestParam BigDecimal location) {
        boolean success = bikeService.updateBikeLocation(bikeId, location);
        Map<String, Boolean> response = new HashMap<>();
        response.put("success", success);
        return success ? ResponseEntity.ok(response) : ResponseEntity.notFound().build();
    }

    // 10. 获取可用车辆
    @GetMapping("/available")
    public List<Bike> getAvailableBikes() {
        return bikeService.getAvailableBikes();
    }

}