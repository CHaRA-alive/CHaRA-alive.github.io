package com.cjy.bikesystem.service;

import com.cjy.bikesystem.entity.Admin;
import com.cjy.bikesystem.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AdminService {

    @Autowired
    private AdminRepository adminRepository;

    // 1. 获取所有管理员
    public List<Admin> getAllAdmins() {
        return adminRepository.findAll();
    }

    // 2. 根据ID获取管理员
    public Optional<Admin> getAdminById(String adminId) {
        return adminRepository.findById(adminId);
    }

    // 3. 添加管理员
    public Admin addAdmin(Admin admin) {
        return adminRepository.save(admin);
    }

    // 4. 更新管理员
    public Admin updateAdmin(String adminId, Admin adminDetails) {
        var admin = adminRepository.findById(adminId);
        if (admin.isPresent()) {
            Admin adminEntity = admin.get();
            adminEntity.setAdminName(adminDetails.getAdminName());
            adminEntity.setAdminPhone(adminDetails.getAdminPhone());
            return adminRepository.save(adminEntity);
        }
        return null;
    }

    // 5. 删除管理员
    public boolean deleteAdmin(String adminId) {
        if (adminRepository.existsById(adminId)) {
            adminRepository.deleteById(adminId);
            return true;
        }
        return false;
    }

    // 6. 根据手机号查找管理员
    public Admin getAdminByPhone(String phone) {
        return adminRepository.findByAdminPhone(phone);
    }

    // 7. 验证管理员登录
    public boolean verifyAdminLogin(String adminId, String phone) {
        var admin = adminRepository.findById(adminId);
        return admin.isPresent() && phone.equals(admin.get().getAdminPhone());
    }
}