package com.cjy.bikesystem.controller;

import com.cjy.bikesystem.entity.MaintenanceWork;
import com.cjy.bikesystem.service.MaintenanceWorkService;
import com.cjy.bikesystem.web.dto.MaintenanceRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/maintenance")
public class MaintenanceWorkController {

    private final MaintenanceWorkService maintenanceWorkService;

    public MaintenanceWorkController(MaintenanceWorkService maintenanceWorkService) {
        this.maintenanceWorkService = maintenanceWorkService;
    }

    @GetMapping
    public List<MaintenanceWork> getAllWorkOrders() {
        return maintenanceWorkService.getAllWorkOrders();
    }

    @PostMapping
    public Map<String, Object> createWorkOrder(@RequestBody MaintenanceRequest request) {
        MaintenanceWork work = maintenanceWorkService.createWorkOrder(request);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("workId", work.getWorkId());
        response.put("work", work);
        return response;
    }

    @GetMapping("/bike/{bikeId}")
    public List<MaintenanceWork> getWorkOrdersByBikeId(@PathVariable String bikeId) {
        return maintenanceWorkService.getWorkOrdersByBikeId(bikeId);
    }

    @GetMapping("/type/{workType}")
    public List<MaintenanceWork> getWorkOrdersByType(@PathVariable String workType) {
        return maintenanceWorkService.getWorkOrdersByType(workType);
    }

    @GetMapping("/status/{handleStatus}")
    public List<MaintenanceWork> getWorkOrdersByStatus(@PathVariable String handleStatus) {
        return maintenanceWorkService.getWorkOrdersByStatus(handleStatus);
    }

    @RequestMapping(value = "/{workId}/status", method = {RequestMethod.PUT, RequestMethod.POST})
    public Map<String, Object> updateWorkOrderStatus(
            @PathVariable String workId,
            @RequestParam String status,
            @RequestParam(defaultValue = "") String result,
            @RequestParam String handlerId
    ) {
        maintenanceWorkService.updateWorkOrderStatus(workId, status, result, handlerId);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        return response;
    }

    @GetMapping("/pending")
    public List<MaintenanceWork> getPendingWorkOrders() {
        return maintenanceWorkService.getPendingWorkOrders();
    }

    @GetMapping("/time-range")
    public List<MaintenanceWork> getWorkOrdersByTimeRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        return maintenanceWorkService.getWorkOrdersByReportTimeRange(start, end);
    }
}
