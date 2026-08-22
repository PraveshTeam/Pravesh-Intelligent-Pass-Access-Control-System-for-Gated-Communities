package com.pravesh.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// One self-referencing entity for both posts and comments: a comment is a row
// with parentPost set and title left null.
@Entity
@Table(name = "forum_posts")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ForumPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Set server-side from the authenticated caller, never from the request body.
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    // Multi-tenancy key: set from the author's JWT societyId claim. Without it,
    // sequential ids would let anyone read/moderate another society's posts.
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "society_id", nullable = false)
    private Society society;

    @Column(length = 30)
    private String category; // null for comment rows

    @Column(length = 150)
    private String title; // posts only, null for comments

    @Column(columnDefinition = "TEXT", nullable = false)
    private String body;

    @Column(name = "is_pinned", nullable = false)
    private boolean pinned;

    // Set on comment rows only.
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_post_id")
    private ForumPost parentPost;

    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "parentPost", fetch = FetchType.LAZY)
    private List<ForumPost> comments = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt; // soft delete for moderation

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.pinned = false;
    }
}
