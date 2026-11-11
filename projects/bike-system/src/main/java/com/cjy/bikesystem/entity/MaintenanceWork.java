package com.cjy.bikesystem.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "MaintenanceWork")
@Data
public class MaintenanceWork {

    @Id
    @Column(name = "WorkID", length = 10)
    private String workId;

    @ManyToOne
    @JoinColumn(name = "BikeID")
    private Bike bike;

    @Column(name = "ReporterID", length = 10)
    private String reporterId;

    @Column(name = "`Desc`", length = 200)
    private String desc;

    @Column(name = "ReportTime")
    private LocalDateTime reportTime;

    @Column(name = "HandleTime")
    private LocalDateTime handleTime;

    @Column(name = "HandlerID", length = 10)
    private String handlerId;

    @Column(name = "HandleStatus", length = 10)
    private String handleStatus;

    @Column(name = "Result", length = 200)
    private String result;

    @Column(name = "WorkType", length = 10)
    private String workType;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public String getBikeId() {
        return bike == null ? null : bike.getBikeId();
    }
}
