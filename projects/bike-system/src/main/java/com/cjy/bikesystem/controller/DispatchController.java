package com.cjy.bikesystem.controller;

import com.cjy.bikesystem.service.DispatchService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dispatch")
public class DispatchController {

    private final DispatchService dispatchService;

    public DispatchController(DispatchService dispatchService) {
        this.dispatchService = dispatchService;
    }

    @GetMapping("/overview")
    public Map<String, Object> overview() {
        return dispatchService.overview();
    }

    @PostMapping("/plans")
    public Map<String, Object> generatePlan() {
        return dispatchService.generatePlan();
    }

    @GetMapping("/plans")
    public List<Map<String, Object>> listPlans() {
        return dispatchService.listPlans();
    }

    @PostMapping("/plans/{planId}/apply")
    public Map<String, Object> applyPlan(@PathVariable String planId) {
        return dispatchService.applyPlan(planId);
    }
}
