package com.cjy.bikesystem.repository;

import com.cjy.bikesystem.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepository extends JpaRepository<Admin, String> {
    Admin findByAdminPhone(String adminPhone);
}