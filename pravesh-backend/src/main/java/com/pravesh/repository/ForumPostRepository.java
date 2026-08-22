package com.pravesh.repository;

import com.pravesh.entity.ForumPost;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ForumPostRepository extends JpaRepository<ForumPost, Long> {

    // Top-level posts for one society, not soft-deleted, pinned first then newest.
    List<ForumPost> findByParentPostIsNullAndSocietyIdAndDeletedAtIsNullOrderByPinnedDescCreatedAtDesc(Long societyId);

    List<ForumPost> findByParentPostIsNullAndSocietyIdAndCategoryAndDeletedAtIsNullOrderByPinnedDescCreatedAtDesc(
            Long societyId, String category);

    // Comments on a post, oldest first. The service checks the parent post's
    // society first, so this can't be reached for another society's post.
    List<ForumPost> findByParentPostIdAndDeletedAtIsNullOrderByCreatedAtAsc(Long parentPostId);

    // Used before any comment/pin/delete: confirms the post exists AND belongs
    // to the caller's own society, which is what closes the IDOR hole.
    Optional<ForumPost> findByIdAndSocietyIdAndDeletedAtIsNull(Long id, Long societyId);
}
