package com.pravesh.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "flats")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Flat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Raw FK column -- authoritative, set once server-side from the admin's
    // own society (JWT), never from client-supplied JSON.
    @Column(name = "society_id", nullable = false)
    private Long societyId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "society_id", insertable = false, updatable = false)
    private Society society;

    @Column(name = "flat_number", nullable = false, length = 20)
    private String flatNumber;

    @Column(length = 20)
    private String tower;

    // NOTE: despite the name, this stores a users.id (the occupying
    // resident's user id), not residents.user_id directly -- see
    // OnboardingService/ResidentRelocationService which call
    // flat.setResidentId(request.getUserId()). It is written directly by
    // services (relocation/onboarding), independently of Resident.flatId,
    // so it stays a normal writable column -- do NOT mark it read-only,
    // that would silently break flat assignment/relocation.
    @Column(name = "resident_id")
    private Long residentId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resident_id", insertable = false, updatable = false)
    private User occupant;
}
