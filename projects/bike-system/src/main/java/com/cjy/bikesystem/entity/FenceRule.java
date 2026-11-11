package com.cjy.bikesystem.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "FenceRule")
@Data
public class FenceRule {

    @Id
    @Column(name = "FenceID", length = 8)
    private String fenceId;

    @Column(name = "FenceType", length = 10)
    private String fenceType;

    @Column(name = "GeoRange", columnDefinition = "TEXT")
    private String geoRange;
}