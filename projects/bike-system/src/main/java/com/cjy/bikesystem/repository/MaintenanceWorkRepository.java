package com.cjy.bikesystem.repository;

import com.cjy.bikesystem.entity.MaintenanceWork;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MaintenanceWorkRepository extends JpaRepository<MaintenanceWork, String> {

    // 根据车辆查询工单
    List<MaintenanceWork> findByBike_BikeId(String bikeId);

    // 根据工单类型查询
    List<MaintenanceWork> findByWorkType(String workType);

    // 根据处理状态查询
    List<MaintenanceWork> findByHandleStatus(String handleStatus);

    // 根据上报时间范围查询
    List<MaintenanceWork> findByReportTimeBetween(LocalDateTime start, LocalDateTime end);

    // 根据处理人查询
    List<MaintenanceWork> findByHandlerId(String handlerId);
}