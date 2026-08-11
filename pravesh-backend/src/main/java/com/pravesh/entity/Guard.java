package com.pravesh.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "guards")
@Getter @Setter
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
public class Guard {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "gate_id", nullable = false, unique = true)
    private Long gateId;

    @Column(name = "employee_code", length = 30)
    private String employeeCode;

    @Column(name = "created_by_admin_id", nullable = false)
    private Long createdByAdminId;
}