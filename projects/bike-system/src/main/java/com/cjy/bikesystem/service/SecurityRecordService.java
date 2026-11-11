package com.cjy.bikesystem.service;

import com.cjy.bikesystem.entity.Bike;
import com.cjy.bikesystem.entity.SecurityRecord;
import com.cjy.bikesystem.exception.BusinessException;
import com.cjy.bikesystem.repository.BikeRepository;
import com.cjy.bikesystem.repository.AdminRepository;
import com.cjy.bikesystem.repository.SecurityPersonRepository;
import com.cjy.bikesystem.repository.SecurityRecordRepository;
import com.cjy.bikesystem.repository.UserRepository;
import com.cjy.bikesystem.web.dto.ViolationRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class SecurityRecordService {

    private final SecurityRecordRepository securityRecordRepository;
    private final BikeRepository bikeRepository;
    private final UserRepository userRepository;
    private final SecurityPersonRepository securityPersonRepository;
    private final AdminRepository adminRepository;

    public SecurityRecordService(
            SecurityRecordRepository securityRecordRepository,
            BikeRepository bikeRepository,
            UserRepository userRepository,
            SecurityPersonRepository securityPersonRepository,
            AdminRepository adminRepository
    ) {
        this.securityRecordRepository = securityRecordRepository;
        this.bikeRepository = bikeRepository;
        this.userRepository = userRepository;
        this.securityPersonRepository = securityPersonRepository;
        this.adminRepository = adminRepository;
    }

    @Transactional
    public SecurityRecord reportViolation(ViolationRequest request) {
        if (request == null) {
            throw new BusinessException("违规记录不能为空");
        }
        String bikeId = required(request.bikeId(), "车辆编号");
        String reporterId = required(request.reporterId(), "上报人编号");
        String position = required(request.violationPosition(), "违规位置");
        Bike bike = bikeRepository.findByBikeIdForUpdate(bikeId)
                .orElseThrow(() -> new BusinessException("车辆不存在"));
        if (!userRepository.existsById(reporterId)
                && !securityPersonRepository.existsById(reporterId)
                && !adminRepository.existsById(reporterId)
                && !"SYSTEM".equals(reporterId)) {
            throw new BusinessException("上报人不存在");
        }

        SecurityRecord record = new SecurityRecord();
        record.setReportId(nextId());
        record.setBike(bike);
        record.setReporterId(reporterId);
        record.setViolationPosition(position);
        record.setViolationType(request.violationType() == null || request.violationType().isBlank()
                ? "其他违规"
                : request.violationType().trim());
        record.setReportTime(LocalDateTime.now());
        record.setHandleStatus("未处理");
        return securityRecordRepository.save(record);
    }

    public List<SecurityRecord> getAllViolations() {
        return securityRecordRepository.findAll();
    }

    public List<SecurityRecord> getViolationsByStatus(String handleStatus) {
        return securityRecordRepository.findByHandleStatus(handleStatus);
    }

    public List<SecurityRecord> getViolationsByReporter(String reporterId) {
        return securityRecordRepository.findByReporterId(reporterId);
    }

    @Transactional
    public boolean handleViolation(String reportId) {
        SecurityRecord record = securityRecordRepository.findById(required(reportId, "记录编号"))
                .orElseThrow(() -> new BusinessException("违规记录不存在"));
        if ("已处理".equals(record.getHandleStatus())) {
            return true;
        }
        record.setHandleStatus("已处理");
        securityRecordRepository.save(record);
        return true;
    }

    public List<SecurityRecord> getUnhandledViolations() {
        return securityRecordRepository.findByHandleStatus("未处理");
    }

    private String nextId() {
        long seed = System.currentTimeMillis() + System.nanoTime();
        String candidate = "R" + String.format("%09d", Math.floorMod(seed, 1_000_000_000L));
        int attempt = 0;
        while (securityRecordRepository.existsById(candidate)) {
            attempt++;
            candidate = "R" + String.format("%09d",
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
