package com.cjy.bikesystem.repository;

import com.cjy.bikesystem.entity.BillRecord;
import com.cjy.bikesystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BillRecordRepository extends JpaRepository<BillRecord, String> {

    // 根据用户查询账单
    List<BillRecord> findByUser(User user);

    // 根据支付方式查询
    List<BillRecord> findByPayMethod(String payMethod);

    // 根据支付时间范围查询
    List<BillRecord> findByPayTimeBetween(LocalDateTime start, LocalDateTime end);

    // 根据信用状态查询
    List<BillRecord> findByCredit(String credit);
}