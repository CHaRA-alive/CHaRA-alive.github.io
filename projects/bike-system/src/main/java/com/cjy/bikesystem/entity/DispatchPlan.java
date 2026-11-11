package com.cjy.bikesystem.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "DispatchPlan")
@Data
public class DispatchPlan {

    @Id
    @Column(name = "PlanID", length = 24)
    private String planId;

    @Column(name = "PlanDate")
    private LocalDateTime planDate;

    @Column(name = "AlgorithmVersion", length = 30)
    private String algorithmVersion;

    @Column(name = "Confidence", precision = 5, scale = 2)
    private BigDecimal confidence;

    @Column(name = "Summary", length = 500)
    private String summary;

    @Lob
    @Column(name = "ItemsJson", columnDefinition = "LONGTEXT")
    private String itemsJson;

    @Column(name = "Status", length = 20)
    private String status;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;

    @Column(name = "AppliedAt")
    private LocalDateTime appliedAt;
}
