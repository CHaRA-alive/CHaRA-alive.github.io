package com.cjy.bikesystem.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "SecurityPerson")
@Data
public class SecurityPerson {

    @Id
    @Column(name = "SecurityID", length = 10)
    private String securityId;

    @Column(name = "SecName", length = 20)
    private String secName;

    @Column(name = "SecPhone", length = 11)
    private String secPhone;

    @Column(name = "Area", length = 50)
    private String area;
}