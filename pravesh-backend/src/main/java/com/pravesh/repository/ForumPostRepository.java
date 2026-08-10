package com.pravesh.repository;

import com.pravesh.entity.ForumPost;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ForumPostRepository extends JpaRepository<ForumPost, Long> {

    // Top-level posts, SCOPED TO ONE SOCIETY, not soft-deleted, pinned first then newest.
    List<ForumPost> findByParentPostIdIsNullAndSocietyIdAndDeletedAtIsNullOrderByPinnedDescCreatedAtDesc(Long societyId);

    List<ForumPost> findByParentPostIdIsNullAndSocietyIdAndCategoryAndDeletedAtIsNullOrderByPinnedDescCreatedAtDesc(
            Long societyId, String category);

    // Comments on a given post, oldest first. Not society-filtered directly --
    // the service layer checks the PARENT post's societyId first (via
    // findByIdAndSocietyIdAndDeletedAtIsNull) before ever calling this, so a
    // comment lookup can't be reached for a post outside the caller's society.
    List<ForumPost> findByParentPostIdAndDeletedAtIsNullOrderByCreatedAtAsc(Long parentPostId);

    // SECURITY-CRITICAL: used before any comment/pin/delete operation. Confirms
    // the post both exists AND belongs to the caller's own society -- this is
    // what closes the IDOR hole (a caller can no longer act on another
    // society's post just by guessing a sequential id).
    Optional<ForumPost> findByIdAndSocietyIdAndDeletedAtIsNull(Long id, Long societyId);
}
