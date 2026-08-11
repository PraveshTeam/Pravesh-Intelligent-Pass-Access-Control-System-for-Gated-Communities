package com.pravesh.entity;

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

    @Column(name = "society_id", nullable = false)
    private Long societyId;

    @Column(name = "flat_number", nullable = false, length = 20)
    private String flatNumber;

    @Column(length = 20)
    private String tower;

    @Column(name = "resident_id")
    private Long residentId;
}