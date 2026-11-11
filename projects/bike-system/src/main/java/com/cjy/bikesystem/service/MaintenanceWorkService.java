package com.cjy.bikesystem.service;

import com.cjy.bikesystem.entity.Bike;
import com.cjy.bikesystem.entity.MaintenanceWork;
import com.cjy.bikesystem.exception.BusinessException;
import com.cjy.bikesystem.repository.BikeRepository;
import com.cjy.bikesystem.repository.AdminRepository;
import com.cjy.bikesystem.repository.MaintenanceWorkRepository;
import com.cjy.bikesystem.repository.SecurityPersonRepository;
import com.cjy.bikesystem.repository.UserRepository;
import com.cjy.bikesystem.web.dto.MaintenanceRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class MaintenanceWorkService {

    private final MaintenanceWorkRepository maintenanceWorkRepository;
    private final BikeRepository bikeRepository;
    private final UserRepository userRepository;
    private final SecurityPersonRepository securityPersonRepository;
    private final AdminRepository adminRepository;

    public MaintenanceWorkService(
            MaintenanceWorkRepository maintenanceWorkRepository,
            BikeRepository bikeRepository,
            UserRepository userRepository,
            SecurityPersonRepository securityPersonRepository,
            AdminRepository adminRepository
    ) {
        this.maintenanceWorkRepository = maintenanceWorkRepository;
        this.bikeRepository = bikeRepository;
        this.userRepository = userRepository;
        this.securityPersonRepository = securityPersonRepository;
        this.adminRepository = adminRepository;
    }

    @Transactional
    public MaintenanceWork createWorkOrder(MaintenanceRequest request) {
        if (request == null) {
            throw new BusinessException("工单内容不能为空");
        }
        String bikeId = required(request.bikeId(), "车辆编号");
        String reporterId = required(request.reporterId(), "上报人编号");
        String desc = required(request.resolvedDescription(), "问题描述");
        if (desc.length() > 200) {
            throw new BusinessException("问题描述不能超过 200 字");
        }

        Bike bike = bikeRepository.findByBikeIdForUpdate(bikeId)
                .orElseThrow(() -> new BusinessException("车辆不存在"));
        boolean validReporter = userRepository.existsById(reporterId)
                || securityPersonRepository.existsById(reporterId)
                || adminRepository.existsById(reporterId)
                || "SYSTEM".equals(reporterId);
        if (!validReporter) {
            throw new BusinessException("上报人不存在");
        }

        String workType = request.workType() == null || request.workType().isBlank()
                ? "维修"
                : request.workType().trim();

        MaintenanceWork workOrder = new MaintenanceWork();
        workOrder.setWorkId(nextId());
        workOrder.setBike(bike);
        workOrder.setReporterId(reporterId);
        workOrder.setDesc(desc);
        workOrder.setReportTime(LocalDateTime.now());
        workOrder.setHandleStatus("待处理");
        workOrder.setWorkType(workType);

        if ("维修".equals(workType)) {
            bike.setBikeStatus("故障");
            bike.setHealthStatus("损坏");
            bikeRepository.save(bike);
        } else if ("清洁".equals(workType)) {
            bike.setHealthStatus("脏污");
            bikeRepository.save(bike);
        } else if ("调度".equals(workType)) {
            bike.setBikeStatus("调度中");
            bikeRepository.save(bike);
        }
        return maintenanceWorkRepository.save(workOrder);
    }

    public List<MaintenanceWork> getAllWorkOrders() {
        return maintenanceWorkRepository.findAll();
    }

    public List<MaintenanceWork> getWorkOrdersByBikeId(String bikeId) {
        return maintenanceWorkRepository.findByBike_BikeId(bikeId);
    }

    public List<MaintenanceWork> getWorkOrdersByType(String workType) {
        return maintenanceWorkRepository.findByWorkType(workType);
    }

    public List<MaintenanceWork> getWorkOrdersByStatus(String handleStatus) {
        return maintenanceWorkRepository.findByHandleStatus(handleStatus);
    }

    @Transactional
    public boolean updateWorkOrderStatus(String workId, String status, String result, String handlerId) {
        MaintenanceWork work = maintenanceWorkRepository.findById(required(workId, "工单编号"))
                .orElseThrow(() -> new BusinessException("工单不存在"));
        String normalizedStatus = required(status, "处理状态");
        if (!List.of("待处理", "处理中", "已完成").contains(normalizedStatus)) {
            throw new BusinessException("不支持的处理状态：" + normalizedStatus);
        }
        if ("已完成".equals(work.getHandleStatus())) {
            throw new BusinessException("已完成工单不能重复处理");
        }

        work.setHandleStatus(normalizedStatus);
        work.setResult(result);
        work.setHandlerId(required(handlerId, "处理人编号"));
        work.setHandleTime(LocalDateTime.now());
        maintenanceWorkRepository.save(work);

        if ("已完成".equals(normalizedStatus) && work.getBike() != null) {
            Bike bike = work.getBike();
            if ("维修".equals(work.getWorkType()) || "清洁".equals(work.getWorkType())) {
                bike.setHealthStatus("正常");
            }
            if (!"使用中".equals(bike.getBikeStatus())) {
                bike.setBikeStatus("可用");
            }
            bikeRepository.save(bike);
        }
        return true;
    }

    public List<MaintenanceWork> getPendingWorkOrders() {
        return maintenanceWorkRepository.findByHandleStatus("待处理");
    }

    public List<MaintenanceWork> getWorkOrdersByReportTimeRange(LocalDateTime start, LocalDateTime end) {
        return maintenanceWorkRepository.findByReportTimeBetween(start, end);
    }

    private String nextId() {
        long seed = System.currentTimeMillis() + System.nanoTime();
        String candidate = "W" + String.format("%09d", Math.floorMod(seed, 1_000_000_000L));
        int attempt = 0;
        while (maintenanceWorkRepository.existsById(candidate)) {
            attempt++;
            candidate = "W" + String.format("%09d",
                    Math.floorMod(seed + attempt * 7919L, 1_000_000_000L));
        }
        return candidate;
    }

    private String required(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(fieldName + "不能为空");
        }
        return value.trim();
    }
}
