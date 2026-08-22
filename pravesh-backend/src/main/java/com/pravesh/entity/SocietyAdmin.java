package com.pravesh.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pravesh.entity.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "society_admins")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class SocietyAdmin {

    // Shared primary key with users.id, derived from the user relationship.
    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    // Set server-side once the society-registration request is approved.
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "society_id")
    private Society society;

    @Column(length = 50)
    private String designation;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 20)
    @Builder.Default
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;
}
