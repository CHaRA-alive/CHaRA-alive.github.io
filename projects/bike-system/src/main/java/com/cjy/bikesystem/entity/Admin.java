package com.cjy.bikesystem.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "Admin")
@Data
public class Admin {

    @Id
    @Column(name = "AdminID", length = 10)
    private String adminId;

    @Column(name = "AdminName", length = 20)
    private String adminName;

    @Column(name = "AdminPhone", length = 11)
    private String adminPhone;
}