package com.cjy.bikesystem.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "User")
@Data
public class User {

    @Id
    @Column(name = "UserID", length = 10)
    private String userId;

    @Column(name = "Age", length = 5)
    private String age;

    @Column(name = "Status", length = 10)
    private String status;

    @Column(name = "Gender", length = 2)
    private String gender;

    @Column(name = "Credit", length = 10)
    private String credit;

    @Column(name = "UserName", length = 20)
    private String userName;

    @Column(name = "Balance", precision = 10, scale = 2)
    private BigDecimal balance;

    @Column(name = "Phone", length = 11, unique = true)
    private String phone;
}