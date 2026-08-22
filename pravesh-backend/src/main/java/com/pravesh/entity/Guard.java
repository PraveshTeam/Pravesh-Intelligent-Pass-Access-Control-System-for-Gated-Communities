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

    // Shared primary key with users.id, derived from the user relationship.
    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gate_id", nullable = false, unique = true)
    private Gate gate;

    @Column(name = "employee_code", length = 30)
    private String employeeCode;

    // Audit fact: which admin created this guard.
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_admin_id", nullable = false)
    private User createdByAdmin;
}
