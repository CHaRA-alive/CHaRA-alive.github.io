package com.cjy.bikesystem.repository;

import com.cjy.bikesystem.entity.SecurityRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SecurityRecordRepository extends JpaRepository<SecurityRecord, String> {
    List<SecurityRecord> findByHandleStatus(String handleStatus);
    List<SecurityRecord> findByReporterId(String reporterId);
}