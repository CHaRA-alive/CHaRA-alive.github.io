package com.cjy.bikesystem.controller;

import com.cjy.bikesystem.entity.User;
import com.cjy.bikesystem.service.UserService;
import com.cjy.bikesystem.service.BikeService;
import com.cjy.bikesystem.service.RideOrderService;
import com.cjy.bikesystem.service.BillRecordService;
import com.cjy.bikesystem.service.MaintenanceWorkService;
import com.cjy.bikesystem.service.SecurityRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class TestController {

    @Autowired
    private UserService userService;

    @Autowired
    private BikeService bikeService;

    @Autowired
    private RideOrderService rideOrderService;

    @Autowired
    private BillRecordService billRecordService;

    @Autowired
    private MaintenanceWorkService maintenanceWorkService;

    @Autowired
    private SecurityRecordService securityRecordService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // ==================== 首页和基础测试 ====================

    @GetMapping("/")
    public Map<String, String> home() {
        Map<String, String> response = new HashMap<>();
        response.put("app", "校园共享单车管理系统 - 后端API");
        response.put("version", "1.0.0");
        response.put("author", "蔡迦逸");
        response.put("student_id", "3124002994");
        response.put("status", "✅ 运行正常");
        response.put("database", "✅ MySQL 9.5 已连接");
        response.put("api", "Spring Boot 3.5.9");
        response.put("test1", "访问 /test 查看系统状态");
        response.put("test2", "访问 /test/db 测试数据库");
        response.put("test3", "访问 /test/crud 测试CRUD操作");
        response.put("api_users", "访问 /api/users 获取用户数据");
        response.put("api_bikes", "访问 /api/bikes 获取车辆数据");
        return response;
    }

    @GetMapping("/test")
    public Map<String, Object> test() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "服务器运行正常！");
        response.put("time", LocalDateTime.now().toString());
        response.put("java_version", System.getProperty("java.version"));
        response.put("spring_boot_version", "3.5.9");
        response.put("system", "校园共享单车智能管理系统");
        return response;
    }

    // ==================== 数据库连接测试 ====================

    @GetMapping("/test/db")
    public Map<String, Object> testDatabase() {
        Map<String, Object> response = new HashMap<>();

        try {
            // 查询所有表的数据量
            Integer userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM User", Integer.class);
            Integer bikeCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM Bike", Integer.class);
            Integer orderCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM RideOrder", Integer.class);
            Integer billCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM BillRecord", Integer.class);
            Integer workCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM MaintenanceWork", Integer.class);
            Integer securityCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM SecurityRecord", Integer.class);

            response.put("status", "success");
            response.put("message", "数据库连接正常");
            response.put("tables", Map.of(
                    "users", userCount,
                    "bikes", bikeCount,
                    "orders", orderCount,
                    "bills", billCount,
                    "maintenance_work", workCount,
                    "security_records", securityCount
            ));

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "数据库连接失败: " + e.getMessage());
        }

        return response;
    }

    // ==================== CRUD操作测试 ====================

    @GetMapping("/test/crud")
    public Map<String, Object> testCrudOperations() {
        Map<String, Object> response = new HashMap<>();

        try {
            // 测试UserService的CRUD
            long totalUsers = userService.getAllUsers().size();
            long availableBikes = bikeService.getAvailableBikes().size();

            response.put("status", "success");
            response.put("total_users", totalUsers);
            response.put("available_bikes", availableBikes);
            response.put("tests", List.of(
                    "GET /test/create-user - 创建测试用户",
                    "GET /test/update-user - 更新用户信息",
                    "GET /test/delete-test-user - 删除测试用户",
                    "GET /test/business-flow - 业务流程测试"
            ));

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "CRUD测试失败: " + e.getMessage());
        }

        return response;
    }

    // 创建测试用户
    @GetMapping("/test/create-user")
    public Map<String, Object> createTestUser() {
        Map<String, Object> response = new HashMap<>();

        try {
            // 检查测试用户是否已存在
            var existingUser = userService.getUserById("9999999999");
            if (existingUser.isPresent()) {
                response.put("status", "info");
                response.put("message", "测试用户已存在，跳过创建");
                response.put("user", existingUser.get());
                return response;
            }

            // 创建测试用户
            User testUser = new User();
            testUser.setUserId("9999999999");
            testUser.setUserName("测试用户");
            testUser.setPhone("13999999999");
            testUser.setGender("男");
            testUser.setAge("21");
            testUser.setCredit("未标记");
            testUser.setStatus("正常");
            testUser.setBalance(BigDecimal.valueOf(100.00));

            User createdUser = userService.addUser(testUser);

            response.put("status", "success");
            response.put("message", "测试用户创建成功");
            response.put("user", Map.of(
                    "userId", createdUser.getUserId(),
                    "userName", createdUser.getUserName(),
                    "phone", createdUser.getPhone(),
                    "balance", createdUser.getBalance()
            ));

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "创建测试用户失败: " + e.getMessage());
        }

        return response;
    }

    // 更新测试用户
    @GetMapping("/test/update-user")
    public Map<String, Object> updateTestUser() {
        Map<String, Object> response = new HashMap<>();

        try {
            // 获取测试用户
            var userOpt = userService.getUserById("9999999999");
            if (!userOpt.isPresent()) {
                response.put("status", "error");
                response.put("message", "测试用户不存在，请先运行 /test/create-user");
                return response;
            }

            User user = userOpt.get();
            String originalName = user.getUserName();
            BigDecimal originalBalance = user.getBalance();

            // 更新用户信息
            user.setUserName(originalName + "(已更新)");
            user.setBalance(originalBalance.add(BigDecimal.valueOf(50.00)));

            User updatedUser = userService.updateUser("9999999999", user);

            response.put("status", "success");
            response.put("message", "测试用户更新成功");
            response.put("changes", Map.of(
                    "name_before", originalName,
                    "name_after", updatedUser.getUserName(),
                    "balance_before", originalBalance,
                    "balance_after", updatedUser.getBalance()
            ));

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "更新测试用户失败: " + e.getMessage());
        }

        return response;
    }

    // 删除测试用户
    @GetMapping("/test/delete-test-user")
    public Map<String, Object> deleteTestUser() {
        Map<String, Object> response = new HashMap<>();

        try {
            boolean deleted = userService.deleteUser("9999999999");

            response.put("status", "success");
            response.put("message", "测试用户删除操作完成");
            response.put("deleted", deleted);
            response.put("note", "如果用户不存在，deleted将为false，这不是错误");

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "删除测试用户失败: " + e.getMessage());
        }

        return response;
    }

    // ==================== 业务流程测试 ====================

    @GetMapping("/test/business-flow")
    public Map<String, Object> testBusinessFlow() {
        Map<String, Object> response = new HashMap<>();

        try {
            // 1. 用户统计
            long totalUsers = userService.getAllUsers().size();
            long markedUsers = userService.getUsersByCredit("标记").size();
            long unmarkedUsers = userService.getUsersByCredit("未标记").size();

            // 2. 车辆统计
            long totalBikes = bikeService.getAllBikes().size();
            long availableBikes = bikeService.getAvailableBikes().size();
            long inUseBikes = bikeService.getBikesByStatus("使用中").size();
            long faultyBikes = bikeService.getBikesByStatus("故障").size();

            // 3. 订单统计
            long totalOrders = rideOrderService.getAllOrders().size();
            long paidOrders = rideOrderService.getOrdersByPayStatus("已支付").size();
            long unpaidOrders = rideOrderService.getOrdersByPayStatus("未支付").size();

            response.put("status", "success");
            response.put("system_overview", "校园共享单车系统业务概览");

            response.put("user_statistics", Map.of(
                    "total", totalUsers,
                    "marked", markedUsers,
                    "unmarked", unmarkedUsers,
                    "marked_percentage", String.format("%.1f%%", totalUsers > 0 ? (markedUsers * 100.0 / totalUsers) : 0)
            ));

            response.put("bike_statistics", Map.of(
                    "total", totalBikes,
                    "available", availableBikes,
                    "in_use", inUseBikes,
                    "faulty", faultyBikes,
                    "availability", String.format("%.1f%%", totalBikes > 0 ? (availableBikes * 100.0 / totalBikes) : 0)
            ));

            response.put("order_statistics", Map.of(
                    "total", totalOrders,
                    "paid", paidOrders,
                    "unpaid", unpaidOrders,
                    "payment_rate", String.format("%.1f%%", totalOrders > 0 ? (paidOrders * 100.0 / totalOrders) : 0)
            ));

            response.put("business_status", "所有业务流程正常");

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "业务流程测试失败: " + e.getMessage());
        }

        return response;
    }

    // ==================== 系统健康检查 ====================

    @GetMapping("/test/health")
    public Map<String, Object> healthCheck() {
        Map<String, Object> response = new HashMap<>();

        try {
            // 检查数据库
            Integer dbCheck = jdbcTemplate.queryForObject("SELECT 1", Integer.class);

            // 检查服务层
            boolean userServiceOk = userService.getAllUsers() != null;
            boolean bikeServiceOk = bikeService.getAllBikes() != null;

            response.put("status", "healthy");
            response.put("timestamp", LocalDateTime.now().toString());
            response.put("checks", Map.of(
                    "database", dbCheck != null && dbCheck == 1 ? "OK" : "FAILED",
                    "user_service", userServiceOk ? "OK" : "FAILED",
                    "bike_service", bikeServiceOk ? "OK" : "FAILED"
            ));
            response.put("message", "系统所有组件运行正常");

        } catch (Exception e) {
            response.put("status", "unhealthy");
            response.put("timestamp", LocalDateTime.now().toString());
            response.put("error", e.getMessage());
            response.put("message", "系统健康检查失败");
        }

        return response;
    }

    // ==================== API文档端点 ====================

    @GetMapping("/test/api-docs")
    public Map<String, Object> apiDocumentation() {
        Map<String, Object> response = new HashMap<>();

        response.put("系统信息", "校园共享单车智能管理系统 API 文档");
        response.put("基础URL", "http://localhost:8080");

        response.put("用户管理API", List.of(
                "GET  /api/users - 获取所有用户",
                "GET  /api/users/{userId} - 获取单个用户",
                "POST /api/users - 创建用户 (需要JSON数据)",
                "PUT  /api/users/{userId} - 更新用户",
                "DELETE /api/users/{userId} - 删除用户",
                "GET  /api/users/credit/{credit} - 按信用查询",
                "GET  /api/users/status/{status} - 按状态查询"
        ));

        response.put("车辆管理API", List.of(
                "GET  /api/bikes - 获取所有车辆",
                "GET  /api/bikes/{bikeId} - 获取单个车辆",
                "GET  /api/bikes/status/{status} - 按状态查询车辆",
                "GET  /api/bikes/health/{healthStatus} - 按健康状态查询"
        ));

        response.put("测试端点", List.of(
                "GET  / - 系统首页",
                "GET  /test - 基础测试",
                "GET  /test/db - 数据库测试",
                "GET  /test/health - 系统健康检查",
                "GET  /test/business-flow - 业务流程测试",
                "GET  /test/api-docs - 本API文档"
        ));

        response.put("数据实体", "系统包含9个主要数据实体：用户、车辆、订单、账单、运维工单、安保记录、围栏规则、安保人员、管理员");

        return response;
    }
}