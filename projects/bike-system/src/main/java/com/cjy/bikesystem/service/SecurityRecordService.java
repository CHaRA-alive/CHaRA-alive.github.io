package com.cjy.bikesystem.service;

import com.cjy.bikesystem.entity.SecurityRecord;
import com.cjy.bikesystem.entity.Bike;
import com.cjy.bikesystem.repository.SecurityRecordRepository;
import com.cjy.bikesystem.repository.BikeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SecurityRecordService {

    @Autowired
    private SecurityRecordRepository securityRecordRepository;

    @Autowired
    private BikeRepository bikeRepository;

    // 1. 上报违规记录
    public SecurityRecord reportViolation(SecurityRecord record) {
        Bike bike = bikeRepository.findByBikeId(record.getBike().getBikeId());
        if (bike != null) {
            record.setBike(bike);
            record.setReportTime(LocalDateTime.now());
            record.setHandleStatus("未处理");
            return securityRecordRepository.save(record);
        }
        return null;
    }

    // 2. 获取所有违规记录
    public List<SecurityRecord> getAllViolations() {
        return securityRecordRepository.findAll();
    }

    // 3. 根据处理状态查询
    public List<SecurityRecord> getViolationsByStatus(String handleStatus) {
        return securityRecordRepository.findByHandleStatus(handleStatus);
    }

    // 4. 根据上报人查询
    public List<SecurityRecord> getViolationsByReporter(String reporterId) {
        return securityRecordRepository.findByReporterId(reporterId);
    }

    // 5. 处理违规记录
    public boolean handleViolation(String reportId) {
        var record = securityRecordRepository.findById(reportId);
        if (record.isPresent()) {
            SecurityRecord securityRecord = record.get();
            securityRecord.setHandleStatus("已处理");
            securityRecordRepository.save(securityRecord);
            return true;
        }
        return false;
    }

    // 6. 获取未处理的违规记录
    public List<SecurityRecord> getUnhandledViolations() {
        return securityRecordRepository.findByHandleStatus("未处理");
    }
}