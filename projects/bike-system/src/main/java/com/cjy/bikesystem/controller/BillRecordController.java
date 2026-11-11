package com.cjy.bikesystem.controller;

import com.cjy.bikesystem.entity.BillRecord;
import com.cjy.bikesystem.service.BillRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bills")
public class BillRecordController {

    @Autowired
    private BillRecordService billRecordService;

    // 1. 获取所有账单
    @GetMapping
    public List<BillRecord> getAllBills() {
        return billRecordService.getAllBills();
    }

    // 2. 创建账单
    @PostMapping
    public ResponseEntity<BillRecord> createBill(@RequestBody BillRecord bill) {
        BillRecord createdBill = billRecordService.createBill(bill);
        return createdBill != null ? ResponseEntity.ok(createdBill) : ResponseEntity.badRequest().build();
    }

    // 3. 根据用户查询账单
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BillRecord>> getBillsByUserId(@PathVariable String userId) {
        List<BillRecord> bills = billRecordService.getBillsByUserId(userId);
        return bills != null ? ResponseEntity.ok(bills) : ResponseEntity.notFound().build();
    }

    // 4. 根据支付方式查询
    @GetMapping("/pay-method/{payMethod}")
    public List<BillRecord> getBillsByPayMethod(@PathVariable String payMethod) {
        return billRecordService.getBillsByPayMethod(payMethod);
    }

    // 5. 根据时间范围查询
    @GetMapping("/time-range")
    public List<BillRecord> getBillsByTimeRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return billRecordService.getBillsByTimeRange(start, end);
    }

    // 6. 根据信用状态查询
    @GetMapping("/credit/{credit}")
    public List<BillRecord> getBillsByCredit(@PathVariable String credit) {
        return billRecordService.getBillsByCredit(credit);
    }

    // 7. 更新账单信用标记
    @PutMapping("/{billId}/credit")
    public ResponseEntity<Map<String, Boolean>> updateBillCredit(
            @PathVariable String billId,
            @RequestParam String credit,
            @RequestParam String reason) {
        boolean success = billRecordService.updateBillCredit(billId, credit, reason);
        Map<String, Boolean> response = new HashMap<>();
        response.put("success", success);
        return success ? ResponseEntity.ok(response) : ResponseEntity.notFound().build();
    }
}