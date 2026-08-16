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

    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    // Raw FK column stays authoritative -- this is what services read/write.
    @Column(name = "flat_id")
    private Long flatId;

    // Read-only relationship view of the same column, for JPA-level joins
    // (e.g. entityManager/JPQL "resident.flat.society.id = :id" instead of
    // manual multi-step lookups). insertable/updatable = false means this
    // side can NEVER be used to change the FK -- only flatId can, and only
    // from service code, never from a client-supplied request body.
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flat_id", insertable = false, updatable = false)
    private Flat flat;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 20)
    @Builder.Default
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @Column(name = "moved_in_date")
    private LocalDate movedInDate;
}
