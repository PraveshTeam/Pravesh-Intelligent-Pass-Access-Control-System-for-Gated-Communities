package com.pravesh.controller;

import com.pravesh.dto.request.CreateCommentRequest;
import com.pravesh.dto.request.CreatePostRequest;
import com.pravesh.dto.response.ApiResponse;
import com.pravesh.dto.response.ForumCommentResponse;
import com.pravesh.dto.response.PostResponse;
import com.pravesh.security.AuthenticatedUser;
import com.pravesh.service.ForumService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/forum")
@RequiredArgsConstructor
public class ForumController {

    private final ForumService forumService;

    @GetMapping("/posts")
    public ResponseEntity<ApiResponse<List<PostResponse>>> listPosts(
            @AuthenticationPrincipal AuthenticatedUser caller,
            @RequestParam(required = false) String category) {
        return ResponseEntity.ok(ApiResponse.ok("Posts", forumService.listPosts(category, caller.societyId())));
    }

    @PostMapping("/posts")
    public ResponseEntity<ApiResponse<PostResponse>> createPost(
            @AuthenticationPrincipal AuthenticatedUser caller,
            @Valid @RequestBody CreatePostRequest req) {
        PostResponse response = forumService.createPost(req, caller.userId(), caller.societyId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Post created", response));
    }

    @GetMapping("/posts/{id}/comments")
    public ResponseEntity<ApiResponse<List<ForumCommentResponse>>> listComments(
            @AuthenticationPrincipal AuthenticatedUser caller,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Comments", forumService.listComments(id, caller.societyId())));
    }

    @PostMapping("/posts/{id}/comments")
    public ResponseEntity<ApiResponse<ForumCommentResponse>> addComment(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser caller,
            @Valid @RequestBody CreateCommentRequest req) {
        ForumCommentResponse response = forumService.addComment(id, req, caller.userId(), caller.societyId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Comment added", response));
    }

    @PutMapping("/posts/{id}/pin")
    @PreAuthorize("hasRole('SOCIETY_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> togglePin(
            @AuthenticationPrincipal AuthenticatedUser caller,
            @PathVariable Long id) {
        forumService.togglePin(id, caller.societyId());
        return ResponseEntity.ok(ApiResponse.ok("Pin status toggled"));
    }

    @DeleteMapping("/posts/{id}")
    @PreAuthorize("hasRole('SOCIETY_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @AuthenticationPrincipal AuthenticatedUser caller,
            @PathVariable Long id) {
        forumService.softDelete(id, caller.societyId());
        return ResponseEntity.ok(ApiResponse.ok("Post removed"));
    }
}
