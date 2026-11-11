package com.cjy.bikesystem.repository;

import com.cjy.bikesystem.entity.FenceRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FenceRuleRepository extends JpaRepository<FenceRule, String> {
    List<FenceRule> findByFenceType(String fenceType);
}