package com.cjy.bikesystem.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "SecurityRecord")
@Data
public class SecurityRecord {

    @Id
    @Column(name = "ReportID", length = 10)
    private String reportId;

    @ManyToOne
    @JoinColumn(name = "ViolationBikeID")
    private Bike bike;

    @Column(name = "ReporterID", length = 10)
    private String reporterId;

    @Column(name = "ViolationPosition", length = 50)
    private String violationPosition;

    @Column(name = "ReportTime")
    private LocalDateTime reportTime;

    @Column(name = "HandleStatus", length = 10)
    private String handleStatus;
}