package com.cjy.bikesystem.controller;

import com.cjy.bikesystem.entity.SecurityRecord;
import com.cjy.bikesystem.service.SecurityRecordService;
import com.cjy.bikesystem.web.dto.ViolationRequest;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/security-records")
public class SecurityRecordController {

    private final SecurityRecordService securityRecordService;

    public SecurityRecordController(SecurityRecordService securityRecordService) {
        this.securityRecordService = securityRecordService;
    }

    @GetMapping
    public List<SecurityRecord> getAllViolations() {
        return securityRecordService.getAllViolations();
    }

    @PostMapping
    public Map<String, Object> reportViolation(@RequestBody ViolationRequest request) {
        SecurityRecord record = securityRecordService.reportViolation(request);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("reportId", record.getReportId());
        response.put("record", record);
        return response;
    }

    @GetMapping("/status/{handleStatus}")
    public List<SecurityRecord> getViolationsByStatus(@PathVariable String handleStatus) {
        return securityRecordService.getViolationsByStatus(handleStatus);
    }

    @GetMapping("/reporter/{reporterId}")
    public List<SecurityRecord> getViolationsByReporter(@PathVariable String reporterId) {
        return securityRecordService.getViolationsByReporter(reporterId);
    }

    @RequestMapping(value = "/{reportId}/handle", method = {RequestMethod.PUT, RequestMethod.POST})
    public Map<String, Object> handleViolation(@PathVariable String reportId) {
        securityRecordService.handleViolation(reportId);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        return response;
    }

    @GetMapping("/unhandled")
    public List<SecurityRecord> getUnhandledViolations() {
        return securityRecordService.getUnhandledViolations();
    }
}
