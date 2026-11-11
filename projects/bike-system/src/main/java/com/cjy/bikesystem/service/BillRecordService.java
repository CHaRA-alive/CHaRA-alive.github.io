package com.cjy.bikesystem.service;

import com.cjy.bikesystem.entity.BillRecord;
import com.cjy.bikesystem.entity.RideOrder;
import com.cjy.bikesystem.entity.User;
import com.cjy.bikesystem.exception.BusinessException;
import com.cjy.bikesystem.repository.BillRecordRepository;
import com.cjy.bikesystem.repository.RideOrderRepository;
import com.cjy.bikesystem.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BillRecordService {

    private final BillRecordRepository billRecordRepository;
    private final RideOrderRepository rideOrderRepository;
    private final UserRepository userRepository;

    public BillRecordService(
            BillRecordRepository billRecordRepository,
            RideOrderRepository rideOrderRepository,
            UserRepository userRepository
    ) {
        this.billRecordRepository = billRecordRepository;
        this.rideOrderRepository = rideOrderRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public BillRecord createBill(BillRecord bill) {
        if (bill == null || bill.getRideOrder() == null || bill.getUser() == null) {
            throw new BusinessException("账单必须关联订单和用户");
        }
        RideOrder order = rideOrderRepository.findById(bill.getRideOrder().getOrderId())
                .orElseThrow(() -> new BusinessException("订单不存在"));
        User user = userRepository.findById(bill.getUser().getUserId())
                .orElseThrow(() -> new BusinessException("用户不存在"));

        bill.setRideOrder(order);
        bill.setUser(user);
        bill.setPayTime(LocalDateTime.now());
        return billRecordRepository.save(bill);
    }

    public List<BillRecord> getAllBills() {
        return billRecordRepository.findAll();
    }

    public List<BillRecord> getBillsByUserId(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        return billRecordRepository.findByUser(user);
    }

    public List<BillRecord> getBillsByPayMethod(String payMethod) {
        return billRecordRepository.findByPayMethod(payMethod);
    }

    public List<BillRecord> getBillsByTimeRange(LocalDateTime start, LocalDateTime end) {
        return billRecordRepository.findByPayTimeBetween(start, end);
    }

    public List<BillRecord> getBillsByCredit(String credit) {
        return billRecordRepository.findByCredit(credit);
    }

    @Transactional
    public boolean updateBillCredit(String billId, String credit, String reason) {
        BillRecord bill = billRecordRepository.findById(billId)
                .orElseThrow(() -> new BusinessException("账单不存在"));
        bill.setCredit(credit);
        bill.setReason(reason);
        billRecordRepository.save(bill);
        return true;
    }
}
