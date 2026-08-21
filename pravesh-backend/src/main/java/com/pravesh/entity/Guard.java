package com.pravesh.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gate_id", insertable = false, updatable = false)
    private Gate gate;

    @Column(name = "employee_code", length = 30)
    private String employeeCode;

    // Raw FK -- authoritative. Kept as plain Long deliberately: the admin
    // who created a guard is an audit fact, not something a guard's own
    // request should ever be able to influence, so no writable relationship
    // is exposed for it.
    @Column(name = "created_by_admin_id", nullable = false)
    private Long createdByAdminId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_admin_id", insertable = false, updatable = false)
    private User createdByAdmin;
}
