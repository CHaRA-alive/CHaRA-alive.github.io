package com.cjy.bikesystem.repository;

import com.cjy.bikesystem.entity.DispatchPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DispatchPlanRepository extends JpaRepository<DispatchPlan, String> {
    List<DispatchPlan> findAllByOrderByCreatedAtDesc();
}
