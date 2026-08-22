package com.pravesh.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "society_id", nullable = false)
    private Society society;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 100)
    private String location;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "gate")
    private Guard guard;
}
