package com.cjy.bikesystem.controller;

import com.cjy.bikesystem.entity.MaintenanceWork;
import com.cjy.bikesystem.service.MaintenanceWorkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/maintenance")
public class MaintenanceWorkController {

    @Autowired
    private MaintenanceWorkService maintenanceWorkService;

    // 1. 获取所有工单
    @GetMapping
    public List<MaintenanceWork> getAllWorkOrders() {
        return maintenanceWorkService.getAllWorkOrders();
    }

    // 2. 创建工单
    @PostMapping
    public ResponseEntity<MaintenanceWork> createWorkOrder(@RequestBody MaintenanceWork workOrder) {
        MaintenanceWork createdOrder = maintenanceWorkService.createWorkOrder(workOrder);
        return createdOrder != null ? ResponseEntity.ok(createdOrder) : ResponseEntity.badRequest().build();
    }

    // 3. 根据车辆查询工单
    @GetMapping("/bike/{bikeId}")
    public List<MaintenanceWork> getWorkOrdersByBikeId(@PathVariable String bikeId) {
        return maintenanceWorkService.getWorkOrdersByBikeId(bikeId);
    }

    // 4. 根据工单类型查询
    @GetMapping("/type/{workType}")
    public List<MaintenanceWork> getWorkOrdersByType(@PathVariable String workType) {
        return maintenanceWorkService.getWorkOrdersByType(workType);
    }

    // 5. 根据处理状态查询
    @GetMapping("/status/{handleStatus}")
    public List<MaintenanceWork> getWorkOrdersByStatus(@PathVariable String handleStatus) {
        return maintenanceWorkService.getWorkOrdersByStatus(handleStatus);
    }

    // 6. 更新工单处理状态
    @PutMapping("/{workId}/status")
    public ResponseEntity<Map<String, Boolean>> updateWorkOrderStatus(
            @PathVariable String workId,
            @RequestParam String status,
            @RequestParam String result,
            @RequestParam String handlerId) {
        boolean success = maintenanceWorkService.updateWorkOrderStatus(workId, status, result, handlerId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("success", success);
        return success ? ResponseEntity.ok(response) : ResponseEntity.notFound().build();
    }

    // 7. 获取待处理工单
    @GetMapping("/pending")
    public List<MaintenanceWork> getPendingWorkOrders() {
        return maintenanceWorkService.getPendingWorkOrders();
    }

    // 8. 根据时间范围查询
    @GetMapping("/time-range")
    public List<MaintenanceWork> getWorkOrdersByTimeRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return maintenanceWorkService.getWorkOrdersByReportTimeRange(start, end); // 修改这里
    }
}