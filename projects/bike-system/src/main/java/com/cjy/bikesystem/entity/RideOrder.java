package com.cjy.bikesystem.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "RideOrder")
@Data
public class RideOrder {

    @Id
    @Column(name = "OrderID", length = 12)
    private String orderId;

    @Column(name = "Fee", precision = 10, scale = 2)
    private BigDecimal fee;

    @ManyToOne
    @JoinColumn(name = "UserID")
    private User user;

    @ManyToOne
    @JoinColumn(name = "BikeID")
    private Bike bike;

    @Column(name = "ParkStatus", length = 10)
    private String parkStatus;

    @Column(name = "StartTime")
    private LocalDateTime startTime;

    @Column(name = "EndTime")
    private LocalDateTime endTime;

    @Column(name = "StartPosition", length = 50)
    private String startPosition;

    @Column(name = "EndPosition", length = 50)
    private String endPosition;

    @Column(name = "PayStatus", length = 10)
    private String payStatus;
}