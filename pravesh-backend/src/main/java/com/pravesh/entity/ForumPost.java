package com.pravesh.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

// Single self-referencing entity for BOTH posts and comments, per the roadmap
// schema -- a comment is just a row with parentPostId set and title left null.
// Trimmed scope: no polls, reactions, or reporting -- posts, comments,
// categories, and pin/soft-delete moderation only.
@Entity
@Table(name = "forum_posts")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ForumPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    // CRITICAL for multi-tenancy: without this, every resident/admin sees every
    // society's forum, and worse -- since ids are sequential, anyone could
    // comment on, pin, or delete a post belonging to a society they're not
    // even a member of, just by guessing/incrementing the id (an IDOR bug).
    // Set once at creation from the author's own JWT societyId claim.
    @Column(name = "society_id", nullable = false)
    private Long societyId;

    @Column(length = 30)
    private String category; // null for comment rows

    @Column(length = 150)
    private String title; // posts only, null for comments

    @Column(columnDefinition = "TEXT", nullable = false)
    private String body;

    @Column(name = "is_pinned", nullable = false)
    private boolean pinned;

    @Column(name = "parent_post_id")
    private Long parentPostId; // set on comment rows only

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
