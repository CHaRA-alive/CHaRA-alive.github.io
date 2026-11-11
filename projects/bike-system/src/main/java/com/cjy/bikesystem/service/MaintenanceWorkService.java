package com.cjy.bikesystem.service;

import com.cjy.bikesystem.entity.MaintenanceWork;
import com.cjy.bikesystem.entity.Bike;
import com.cjy.bikesystem.repository.MaintenanceWorkRepository;
import com.cjy.bikesystem.repository.BikeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MaintenanceWorkService {

    @Autowired
    private MaintenanceWorkRepository maintenanceWorkRepository;

    @Autowired
    private BikeRepository bikeRepository;

    // 1. 创建运维工单
    public MaintenanceWork createWorkOrder(MaintenanceWork workOrder) {
        Bike bike = bikeRepository.findByBikeId(workOrder.getBike().getBikeId());
        if (bike != null) {
            workOrder.setBike(bike);
            workOrder.setReportTime(LocalDateTime.now());
            workOrder.setHandleStatus("待处理");
            return maintenanceWorkRepository.save(workOrder);
        }
        return null;
    }

    // 2. 获取所有工单
    public List<MaintenanceWork> getAllWorkOrders() {
        return maintenanceWorkRepository.findAll();
    }

    // 3. 根据车辆查询工单
    public List<MaintenanceWork> getWorkOrdersByBikeId(String bikeId) {
        return maintenanceWorkRepository.findByBike_BikeId(bikeId);
    }

    // 4. 根据工单类型查询
    public List<MaintenanceWork> getWorkOrdersByType(String workType) {
        return maintenanceWorkRepository.findByWorkType(workType);
    }

    // 5. 根据处理状态查询
    public List<MaintenanceWork> getWorkOrdersByStatus(String handleStatus) {
        return maintenanceWorkRepository.findByHandleStatus(handleStatus);
    }

    // 6. 更新工单处理状态
    public boolean updateWorkOrderStatus(String workId, String status, String result, String handlerId) {
        var workOrder = maintenanceWorkRepository.findById(workId);
        if (workOrder.isPresent()) {
            MaintenanceWork work = workOrder.get();
            work.setHandleStatus(status);
            work.setResult(result);
            work.setHandlerId(handlerId);
            work.setHandleTime(LocalDateTime.now());
            maintenanceWorkRepository.save(work);

            // 如果是维修完成，更新车辆状态
            if ("已完成".equals(status) && "维修".equals(work.getWorkType())) {
                Bike bike = work.getBike();
                bike.setHealthStatus("正常");
                bikeRepository.save(bike);
            }
            return true;
        }
        return false;
    }

    // 7. 获取待处理工单
    public List<MaintenanceWork> getPendingWorkOrders() {
        return maintenanceWorkRepository.findByHandleStatus("待处理");
    }

    // 8. 根据时间范围查询 - 修复这里的方法名
    public List<MaintenanceWork> getWorkOrdersByReportTimeRange(LocalDateTime start, LocalDateTime end) {
        return maintenanceWorkRepository.findByReportTimeBetween(start, end);
    }
}