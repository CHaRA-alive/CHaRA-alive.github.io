package com.cjy.bikesystem.service;

import com.cjy.bikesystem.entity.BillRecord;
import com.cjy.bikesystem.entity.RideOrder;
import com.cjy.bikesystem.entity.User;
import com.cjy.bikesystem.repository.BillRecordRepository;
import com.cjy.bikesystem.repository.RideOrderRepository;
import com.cjy.bikesystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BillRecordService {

    @Autowired
    private BillRecordRepository billRecordRepository;

    @Autowired
    private RideOrderRepository rideOrderRepository;

    @Autowired
    private UserRepository userRepository;

    // 1. 创建账单记录
    public BillRecord createBill(BillRecord bill) {
        // 验证订单和用户是否存在
        Optional<RideOrder> order = rideOrderRepository.findById(bill.getRideOrder().getOrderId());
        Optional<User> user = userRepository.findById(bill.getUser().getUserId());

        if (order.isPresent() && user.isPresent()) {
            bill.setRideOrder(order.get());
            bill.setUser(user.get());
            bill.setPayTime(LocalDateTime.now());
            return billRecordRepository.save(bill);
        }
        return null;
    }

    // 2. 获取所有账单
    public List<BillRecord> getAllBills() {
        return billRecordRepository.findAll();
    }

    // 3. 根据用户查询账单
    public List<BillRecord> getBillsByUserId(String userId) {
        Optional<User> user = userRepository.findById(userId);
        return user.map(billRecordRepository::findByUser).orElse(null);
    }

    // 4. 根据支付方式查询
    public List<BillRecord> getBillsByPayMethod(String payMethod) {
        return billRecordRepository.findByPayMethod(payMethod);
    }

    // 5. 根据时间范围查询
    public List<BillRecord> getBillsByTimeRange(LocalDateTime start, LocalDateTime end) {
        return billRecordRepository.findByPayTimeBetween(start, end);
    }

    // 6. 根据信用状态查询
    public List<BillRecord> getBillsByCredit(String credit) {
        return billRecordRepository.findByCredit(credit);
    }

    // 7. 更新账单信用标记
    public boolean updateBillCredit(String billId, String credit, String reason) {
        Optional<BillRecord> bill = billRecordRepository.findById(billId);
        if (bill.isPresent()) {
            BillRecord billRecord = bill.get();
            billRecord.setCredit(credit);
            billRecord.setReason(reason);
            billRecordRepository.save(billRecord);
            return true;
        }
        return false;
    }
}