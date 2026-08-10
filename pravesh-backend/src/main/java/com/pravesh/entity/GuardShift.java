package com.pravesh.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "guard_shifts")
@Getter @Setter
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
public class GuardShift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "guard_user_id", nullable = false)
    private Long guardUserId;

    @Column(name = "gate_id", nullable = false)
    private Long gateId;

    @Column(name = "on_duty_name", nullable = false, length = 100)
    private String onDutyName;

    @Column(name = "on_duty_employee_id", length = 30)
    private String onDutyEmployeeId;

    @Column(name = "shift_start", nullable = false)
    private LocalDateTime shiftStart;

    @Column(name = "shift_end")
    private LocalDateTime shiftEnd;

    @PrePersist
    protected void onCreate() {
        this.shiftStart = LocalDateTime.now();
    }
}