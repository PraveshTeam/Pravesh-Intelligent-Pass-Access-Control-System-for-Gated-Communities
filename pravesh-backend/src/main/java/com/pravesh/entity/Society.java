package com.pravesh.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "societies")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Society {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 255)
    private String address;

    @Column(length = 50)
    private String city;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ---- Relationships (read-only navigation, LAZY) --------------------
    // These are additive: every existing "societyId" foreign-key column
    // elsewhere in the codebase is UNCHANGED and remains the value that is
    // actually written/read by services. These collections just let you
    // navigate society -> children via JPA instead of writing manual
    // "findBySocietyId(...)" chains everywhere. They are never populated
    // from client input and are excluded from JSON responses, so they
    // cannot be used to leak or reassign a resource across tenants.
    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "society", fetch = FetchType.LAZY)
    private List<Flat> flats = new ArrayList<>();

    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "society", fetch = FetchType.LAZY)
    private List<Gate> gates = new ArrayList<>();

    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "society", fetch = FetchType.LAZY)
    private List<VisitorPass> visitorPasses = new ArrayList<>();

    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "society", fetch = FetchType.LAZY)
    private List<EntryLog> entryLogs = new ArrayList<>();

    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "society", fetch = FetchType.LAZY)
    private List<Trip> trips = new ArrayList<>();

    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "society", fetch = FetchType.LAZY)
    private List<ForumPost> forumPosts = new ArrayList<>();

    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "society", fetch = FetchType.LAZY)
    private List<SosAlert> sosAlerts = new ArrayList<>();

    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "society", fetch = FetchType.LAZY)
    private List<SocietyAdmin> admins = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
