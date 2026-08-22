package com.pravesh.entity;

import com.pravesh.entity.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "society_admins")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class SocietyAdmin {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "society_id")
    private Long societyId; 

    @Column(length = 50)
    private String designation;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 20)
    @Builder.Default
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;
}