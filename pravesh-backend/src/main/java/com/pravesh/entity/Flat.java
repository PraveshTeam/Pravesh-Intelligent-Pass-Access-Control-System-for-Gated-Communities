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

    // Set once server-side from the admin's own society (JWT), never from client JSON.
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "society_id", nullable = false)
    private Society society;

    @Column(name = "flat_number", nullable = false, length = 20)
    private String flatNumber;

    @Column(length = 20)
    private String tower;

    // Column is resident_id but holds a users.id -- the occupying resident's user.
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resident_id")
    private User occupant;
}
