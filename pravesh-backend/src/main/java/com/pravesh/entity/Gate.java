package com.pravesh.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "gates")
@Getter @Setter
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
public class Gate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "society_id", nullable = false)
    private Long societyId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 100)
    private String location;
}