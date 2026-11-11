package com.cjy.bikesystem.controller;

import com.cjy.bikesystem.entity.Admin;
import com.cjy.bikesystem.entity.User;
import com.cjy.bikesystem.entity.SecurityPerson;
import com.cjy.bikesystem.service.AdminService;
import com.cjy.bikesystem.service.UserService;
import com.cjy.bikesystem.service.SecurityPersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class LoginController {

    @Autowired
    private UserService userService;

    @Autowired
    private AdminService adminService;

    @Autowired
    private SecurityPersonService securityPersonService;

    // 用户登录 - 根据SQL，使用UserID和Phone验证
    @PostMapping("/user/login")
    public ResponseEntity<Map<String, Object>> userLogin(
            @RequestParam String userId,
            @RequestParam String phone) {

        Map<String, Object> response = new HashMap<>();

        try {
            var user = userService.getUserById(userId);

            if (user.isPresent() && phone.equals(user.get().getPhone())) {
                response.put("success", true);
                response.put("userType", "user");
                response.put("user", user.get());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "用户ID或手机号错误");
                return ResponseEntity.status(401).body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "登录失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // 管理员登录 - 包括系统管理员和运维人员
    @PostMapping("/admin/login")
    public ResponseEntity<Map<String, Object>> adminLogin(
            @RequestParam String adminId,
            @RequestParam String phone) {

        Map<String, Object> response = new HashMap<>();

        try {
            var adminOpt = adminService.getAdminById(adminId);

            if (adminOpt.isPresent()) {
                Admin admin = adminOpt.get();

                // 验证手机号
                if (admin.getAdminPhone().equals(phone)) {
                    response.put("success", true);
                    response.put("userType", "admin");
                    response.put("admin", admin);

                    // 判断是否为运维人员（ID以O开头）
                    if (adminId.startsWith("O")) {
                        response.put("role", "operator");
                        response.put("displayName", admin.getAdminName() + "（运维）");
                    } else {
                        response.put("role", "admin");
                        response.put("displayName", admin.getAdminName() + "（管理员）");
                    }

                    return ResponseEntity.ok(response);
                }
            }

            response.put("success", false);
            response.put("message", "管理员ID或手机号错误");
            return ResponseEntity.status(401).body(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "登录失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // 安保人员登录
    @PostMapping("/security/login")
    public ResponseEntity<Map<String, Object>> securityLogin(
            @RequestParam String securityId,
            @RequestParam String phone) {

        Map<String, Object> response = new HashMap<>();

        try {
            var personOpt = securityPersonService.getSecurityPersonById(securityId);

            if (personOpt.isPresent()) {
                SecurityPerson person = personOpt.get();

                if (person.getSecPhone().equals(phone)) {
                    response.put("success", true);
                    response.put("userType", "security");
                    response.put("securityPerson", person);
                    return ResponseEntity.ok(response);
                }
            }

            response.put("success", false);
            response.put("message", "安保人员ID或手机号错误");
            return ResponseEntity.status(401).body(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "登录失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // 检查所有登录接口状态
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testAuth() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "auth_controller_ready");
        response.put("timestamp", System.currentTimeMillis());
        response.put("endpoints", new String[] {
                "POST /api/auth/user/login - 用户登录",
                "POST /api/auth/admin/login - 管理员/运维登录",
                "POST /api/auth/security/login - 安保人员登录"
        });
        return ResponseEntity.ok(response);
    }
}