package com.cjy.bikesystem.controller;

import com.cjy.bikesystem.entity.FenceRule;
import com.cjy.bikesystem.service.FenceRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/fence-rules")
public class FenceRuleController {

    @Autowired
    private FenceRuleService fenceRuleService;

    // 1. 获取所有区域规则
    @GetMapping
    public List<FenceRule> getAllFenceRules() {
        return fenceRuleService.getAllFenceRules();
    }

    // 2. 添加区域规则
    @PostMapping
    public FenceRule addFenceRule(@RequestBody FenceRule rule) {
        return fenceRuleService.addFenceRule(rule);
    }

    // 3. 更新区域规则
    @PutMapping("/{fenceId}")
    public ResponseEntity<FenceRule> updateFenceRule(@PathVariable String fenceId, @RequestBody FenceRule ruleDetails) {
        FenceRule updatedRule = fenceRuleService.updateFenceRule(fenceId, ruleDetails);
        return updatedRule != null ? ResponseEntity.ok(updatedRule) : ResponseEntity.notFound().build();
    }

    // 4. 删除区域规则
    @DeleteMapping("/{fenceId}")
    public ResponseEntity<Map<String, Boolean>> deleteFenceRule(@PathVariable String fenceId) {
        boolean deleted = fenceRuleService.deleteFenceRule(fenceId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("deleted", deleted);
        return deleted ? ResponseEntity.ok(response) : ResponseEntity.notFound().build();
    }

    // 5. 获取禁停区列表
    @GetMapping("/no-parking")
    public List<FenceRule> getNoParkingZones() {
        return fenceRuleService.getNoParkingZones();
    }

    // 6. 获取推荐停车区列表
    @GetMapping("/recommended")
    public List<FenceRule> getRecommendedZones() {
        return fenceRuleService.getRecommendedZones();
    }

    // 7. 根据类型查询
    @GetMapping("/type/{fenceType}")
    public List<FenceRule> getFenceRulesByType(@PathVariable String fenceType) {
        return fenceRuleService.getFenceRulesByType(fenceType);
    }
}