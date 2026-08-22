package com.pravesh.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pravesh.entity.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "residents")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resident {

    // Shared primary key with users.id, derived from the user relationship.
    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flat_id")
    private Flat flat;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 20)
    @Builder.Default
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @Column(name = "moved_in_date")
    private LocalDate movedInDate;
}
