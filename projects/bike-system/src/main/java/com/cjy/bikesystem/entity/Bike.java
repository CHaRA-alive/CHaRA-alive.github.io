package com.cjy.bikesystem.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "Bike")
@Data
public class Bike {

    @Id
    @Column(name = "BikeID", length = 8)
    private String bikeId;

    @Column(name = "QRCode", length = 20)
    private String qrCode;

    @Column(name = "BikeStatus", length = 10)
    private String bikeStatus;

    @Column(name = "HealthStatus", length = 10)
    private String healthStatus;

    // 修改这里：DECIMAL(10,6) 用 BigDecimal
    @Column(name = "Location", precision = 10, scale = 6)
    private BigDecimal location;

    @Column(name = "Area", length = 50)
    private String area;
}
