package com.cjy.bikesystem.controller;

import com.cjy.bikesystem.entity.SecurityRecord;
import com.cjy.bikesystem.service.SecurityRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/security-records")
public class SecurityRecordController {

    @Autowired
    private SecurityRecordService securityRecordService;

    // 1. 获取所有违规记录
    @GetMapping
    public List<SecurityRecord> getAllViolations() {
        return securityRecordService.getAllViolations();
    }

    // 2. 上报违规记录
    @PostMapping
    public ResponseEntity<SecurityRecord> reportViolation(@RequestBody SecurityRecord record) {
        SecurityRecord reportedRecord = securityRecordService.reportViolation(record);
        return reportedRecord != null ? ResponseEntity.ok(reportedRecord) : ResponseEntity.badRequest().build();
    }

    // 3. 根据处理状态查询
    @GetMapping("/status/{handleStatus}")
    public List<SecurityRecord> getViolationsByStatus(@PathVariable String handleStatus) {
        return securityRecordService.getViolationsByStatus(handleStatus);
    }

    // 4. 根据上报人查询
    @GetMapping("/reporter/{reporterId}")
    public List<SecurityRecord> getViolationsByReporter(@PathVariable String reporterId) {
        return securityRecordService.getViolationsByReporter(reporterId);
    }

    // 5. 处理违规记录
    @PutMapping("/{reportId}/handle")
    public ResponseEntity<Map<String, Boolean>> handleViolation(@PathVariable String reportId) {
        boolean success = securityRecordService.handleViolation(reportId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("success", success);
        return success ? ResponseEntity.ok(response) : ResponseEntity.notFound().build();
    }

    // 6. 获取未处理的违规记录
    @GetMapping("/unhandled")
    public List<SecurityRecord> getUnhandledViolations() {
        return securityRecordService.getUnhandledViolations();
    }
}