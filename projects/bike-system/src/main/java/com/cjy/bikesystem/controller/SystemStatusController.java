package com.cjy.bikesystem.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
public class SystemStatusController {

    private final JdbcTemplate jdbcTemplate;

    public SystemStatusController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/api/info")
    public Map<String, Object> info() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("software", "青骑智行校园共享单车智能运营系统");
        response.put("version", "1.0.0");
        response.put("status", "running");
        response.put("modules", List.of(
                "用户骑行与计费",
                "车辆与运维工单",
                "安全违规治理",
                "运营数据分析",
                "时空供需智能调度"
        ));
        response.put("timestamp", LocalDateTime.now());
        return response;
    }

    @GetMapping("/test")
    public Map<String, Object> status() {
        return health();
    }

    @GetMapping("/test/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            Integer databaseCheck = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            response.put("status", "healthy");
            response.put("database", databaseCheck != null && databaseCheck == 1 ? "OK" : "FAILED");
        } catch (RuntimeException exception) {
            response.put("status", "unhealthy");
            response.put("database", "FAILED");
            response.put("message", "数据库健康检查失败");
        }
        response.put("timestamp", LocalDateTime.now());
        return response;
    }
}
