package com.cjy.bikesystem.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "BillRecord")
@Data
public class BillRecord {

    @Id
    @Column(name = "BillID", length = 12)
    private String billId;

    @OneToOne
    @JoinColumn(name = "OrderID")
    private RideOrder rideOrder;

    @Column(name = "Amount", precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "PayMethod", length = 10)
    private String payMethod;

    @Column(name = "PayTime")
    private LocalDateTime payTime;

    @Column(name = "Reason", length = 50)
    private String reason;

    @Column(name = "Credit", length = 10)
    private String credit;

    @ManyToOne
    @JoinColumn(name = "UserID")
    private User user;
}