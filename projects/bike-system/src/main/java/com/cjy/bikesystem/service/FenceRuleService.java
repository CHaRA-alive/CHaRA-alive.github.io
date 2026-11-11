package com.cjy.bikesystem.service;

import com.cjy.bikesystem.entity.FenceRule;
import com.cjy.bikesystem.repository.FenceRuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FenceRuleService {

    @Autowired
    private FenceRuleRepository fenceRuleRepository;

    // 1. 添加区域规则
    public FenceRule addFenceRule(FenceRule rule) {
        return fenceRuleRepository.save(rule);
    }

    // 2. 获取所有区域规则
    public List<FenceRule> getAllFenceRules() {
        return fenceRuleRepository.findAll();
    }

    // 3. 根据区域类型查询
    public List<FenceRule> getFenceRulesByType(String fenceType) {
        return fenceRuleRepository.findByFenceType(fenceType);
    }

    // 4. 更新区域规则
    public FenceRule updateFenceRule(String fenceId, FenceRule ruleDetails) {
        var rule = fenceRuleRepository.findById(fenceId);
        if (rule.isPresent()) {
            FenceRule fenceRule = rule.get();
            fenceRule.setFenceType(ruleDetails.getFenceType());
            fenceRule.setGeoRange(ruleDetails.getGeoRange());
            return fenceRuleRepository.save(fenceRule);
        }
        return null;
    }

    // 5. 删除区域规则
    public boolean deleteFenceRule(String fenceId) {
        if (fenceRuleRepository.existsById(fenceId)) {
            fenceRuleRepository.deleteById(fenceId);
            return true;
        }
        return false;
    }

    // 6. 获取禁停区列表
    public List<FenceRule> getNoParkingZones() {
        return fenceRuleRepository.findByFenceType("禁停区");
    }

    // 7. 获取推荐停车区列表
    public List<FenceRule> getRecommendedZones() {
        return fenceRuleRepository.findByFenceType("推荐区");
    }
}