package com.pravesh.service;

import com.pravesh.dto.request.CreateCommentRequest;
import com.pravesh.dto.request.CreatePostRequest;
import com.pravesh.dto.response.ForumCommentResponse;
import com.pravesh.dto.response.PostResponse;
import com.pravesh.entity.ForumPost;
import com.pravesh.exception.InvalidStateException;
import com.pravesh.exception.ResourceNotFoundException;
import com.pravesh.dto.response.UserContactResponse;
import com.pravesh.repository.ForumPostRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ForumService {

    private static final Logger log = LoggerFactory.getLogger(ForumService.class);

    private final ForumPostRepository postRepository;
    private final com.pravesh.service.UserDirectoryService userDirectoryService;


    // SECURITY-CRITICAL: scoped to the caller's own society. societyId comes
    // from the caller's JWT (X-Society-Id header set by the gateway), never
    // from a request parameter, so nobody can pass a different society's id
    // to browse a forum they don't belong to.
    public List<PostResponse> listPosts(String category, Long societyId) {
        List<ForumPost> posts = (category == null || category.isBlank())
                ? postRepository.findByParentPostIdIsNullAndSocietyIdAndDeletedAtIsNullOrderByPinnedDescCreatedAtDesc(societyId)
                : postRepository.findByParentPostIdIsNullAndSocietyIdAndCategoryAndDeletedAtIsNullOrderByPinnedDescCreatedAtDesc(societyId, category);

        Map<Long, String> authorNames = resolveAuthorNames(posts.stream().map(ForumPost::getAuthorId).collect(Collectors.toSet()));

        return posts.stream().map(p -> toPostResponse(p, authorNames)).toList();
    }

    @Transactional
    public PostResponse createPost(CreatePostRequest req, Long authorId, Long societyId) {
        if (societyId == null) {
            throw new InvalidStateException("Could not determine your society. Please log in again.");
        }

        ForumPost post = ForumPost.builder()
                .authorId(authorId)
                .societyId(societyId)
                .category(req.category())
                .title(req.title())
                .body(req.body())
                .build();
        post = postRepository.save(post);

        Map<Long, String> authorNames = resolveAuthorNames(Set.of(authorId));
        return toPostResponse(post, authorNames);
    }

    public List<ForumCommentResponse> listComments(Long postId, Long callerSocietyId) {
        // This lookup being society-scoped is what prevents anyone from listing
        // comments on a post that belongs to a different society, even if they
        // know/guess its id.
        postRepository.findByIdAndSocietyIdAndDeletedAtIsNull(postId, callerSocietyId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found: " + postId));

        List<ForumPost> comments = postRepository.findByParentPostIdAndDeletedAtIsNullOrderByCreatedAtAsc(postId);
        Map<Long, String> authorNames = resolveAuthorNames(comments.stream().map(ForumPost::getAuthorId).collect(Collectors.toSet()));

        return comments.stream().map(c -> toCommentResponse(c, authorNames)).toList();
    }

    @Transactional
    public ForumCommentResponse addComment(Long postId, CreateCommentRequest req, Long authorId, Long callerSocietyId) {
        ForumPost parent = postRepository.findByIdAndSocietyIdAndDeletedAtIsNull(postId, callerSocietyId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found: " + postId));

        ForumPost comment = ForumPost.builder()
                .authorId(authorId)
                .societyId(callerSocietyId) // inherits the parent's society, not re-derived
                .parentPostId(parent.getId())
                .body(req.body())
                .build();
        comment = postRepository.save(comment);

        Map<Long, String> authorNames = resolveAuthorNames(Set.of(authorId));
        return toCommentResponse(comment, authorNames);
    }

    @Transactional
    public void togglePin(Long postId, Long adminSocietyId) {
        // An admin can only pin/unpin posts in their OWN society -- this closes
        // the cross-society moderation hole (an admin acting on another
        // society's post just by knowing its id).
        ForumPost post = postRepository.findByIdAndSocietyIdAndDeletedAtIsNull(postId, adminSocietyId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found: " + postId));
        if (post.getParentPostId() != null) {
            throw new InvalidStateException("Only top-level posts can be pinned, not comments");
        }
        post.setPinned(!post.isPinned());
        postRepository.save(post);
    }

    @Transactional
    public void softDelete(Long postId, Long adminSocietyId) {
        ForumPost post = postRepository.findByIdAndSocietyIdAndDeletedAtIsNull(postId, adminSocietyId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found: " + postId));
        post.setDeletedAt(LocalDateTime.now());
        postRepository.save(post);
    }

    private Map<Long, String> resolveAuthorNames(Set<Long> authorIds) {
        Map<Long, String> names = new HashMap<>();
        for (Long id : authorIds) {
            try {
                UserContactResponse contact = userDirectoryService.getContact(id);
                if (contact != null) {
                    names.put(id, contact.name());
                }
            } catch (Exception e) {
                log.warn("Could not resolve author name for user {}: {}", id, e.getMessage());
            }
        }
        return names;
    }

    private PostResponse toPostResponse(ForumPost p, Map<Long, String> authorNames) {
        int commentCount = postRepository.findByParentPostIdAndDeletedAtIsNullOrderByCreatedAtAsc(p.getId()).size();
        return new PostResponse(
                p.getId(), p.getAuthorId(), authorNames.get(p.getAuthorId()),
                p.getCategory(), p.getTitle(), p.getBody(), p.isPinned(),
                commentCount, p.getCreatedAt());
    }

    private ForumCommentResponse toCommentResponse(ForumPost c, Map<Long, String> authorNames) {
        return new ForumCommentResponse(
                c.getId(), c.getAuthorId(), authorNames.get(c.getAuthorId()),
                c.getBody(), c.getCreatedAt());
    }
}
