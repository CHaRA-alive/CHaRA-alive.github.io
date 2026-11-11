package com.cjy.bikesystem.controller;

import com.cjy.bikesystem.entity.Admin;
import com.cjy.bikesystem.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admins")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @GetMapping
    public List<Admin> getAllAdmins() {
        return adminService.getAllAdmins();
    }

    @GetMapping("/{adminId}")
    public ResponseEntity<Admin> getAdminById(@PathVariable String adminId) {
        var admin = adminService.getAdminById(adminId);
        return admin.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Admin addAdmin(@RequestBody Admin admin) {
        return adminService.addAdmin(admin);
    }

    @PutMapping("/{adminId}")
    public ResponseEntity<Admin> updateAdmin(@PathVariable String adminId, @RequestBody Admin adminDetails) {
        Admin updatedAdmin = adminService.updateAdmin(adminId, adminDetails);
        return updatedAdmin != null ? ResponseEntity.ok(updatedAdmin) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{adminId}")
    public ResponseEntity<Map<String, Boolean>> deleteAdmin(@PathVariable String adminId) {
        boolean deleted = adminService.deleteAdmin(adminId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("deleted", deleted);
        return deleted ? ResponseEntity.ok(response) : ResponseEntity.notFound().build();
    }
}