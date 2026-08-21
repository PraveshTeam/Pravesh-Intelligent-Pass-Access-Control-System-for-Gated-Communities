package com.pravesh.controller;

import com.pravesh.dto.request.AddCommentRequest;
import com.pravesh.dto.request.ProposeTripRequest;
import com.pravesh.dto.request.RequestDecisionRequest;
import com.pravesh.dto.response.ApiResponse;
import com.pravesh.dto.response.TripCommentResponse;
import com.pravesh.dto.response.JoinRequestResponse;
import com.pravesh.dto.response.TripResponse;
import com.pravesh.security.AuthenticatedUser;
import com.pravesh.service.TripService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
<<<<<<< HEAD
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
=======
>>>>>>> origin/salonee
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;

    @GetMapping
<<<<<<< HEAD
    public ResponseEntity<ApiResponse<List<TripResponse>>> listTrips(@AuthenticationPrincipal AuthenticatedUser caller) {
        return ResponseEntity.ok(ApiResponse.ok("Trips", tripService.listTrips(caller.societyId(), caller.userId())));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TripResponse>> proposeTrip(
            @AuthenticationPrincipal AuthenticatedUser caller,
            @Valid @RequestBody ProposeTripRequest req) {
        TripResponse response = tripService.proposeTrip(req, caller.userId(), caller.societyId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Trip proposed", response));
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<ApiResponse<JoinRequestResponse>> requestToJoin(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser caller) {
        JoinRequestResponse response = tripService.requestToJoin(id, caller.userId(), caller.societyId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Join request sent", response));
    }

    @GetMapping("/{id}/requests")
    public ResponseEntity<ApiResponse<List<JoinRequestResponse>>> listRequests(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser caller) {
        return ResponseEntity.ok(ApiResponse.ok("Join requests", tripService.listRequests(id, caller.userId(), caller.societyId())));
    }

    @PutMapping("/{id}/requests/{reqId}")
    public ResponseEntity<ApiResponse<JoinRequestResponse>> decideRequest(
=======
    public ApiResponse<List<TripResponse>> listTrips(@AuthenticationPrincipal AuthenticatedUser caller) {
        return ApiResponse.ok("Trips", tripService.listTrips(caller.societyId(), caller.userId()));
    }

    @PostMapping
    public ApiResponse<TripResponse> proposeTrip(
            @AuthenticationPrincipal AuthenticatedUser caller,
            @Valid @RequestBody ProposeTripRequest req) {
        return ApiResponse.ok("Trip proposed", tripService.proposeTrip(req, caller.userId(), caller.societyId()));
    }

    @PostMapping("/{id}/join")
    public ApiResponse<JoinRequestResponse> requestToJoin(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser caller) {
        return ApiResponse.ok("Join request sent", tripService.requestToJoin(id, caller.userId(), caller.societyId()));
    }

    @GetMapping("/{id}/requests")
    public ApiResponse<List<JoinRequestResponse>> listRequests(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser caller) {
        return ApiResponse.ok("Join requests", tripService.listRequests(id, caller.userId(), caller.societyId()));
    }

    @PutMapping("/{id}/requests/{reqId}")
    public ApiResponse<JoinRequestResponse> decideRequest(
>>>>>>> origin/salonee
            @PathVariable Long id,
            @PathVariable Long reqId,
            @AuthenticationPrincipal AuthenticatedUser caller,
            @Valid @RequestBody RequestDecisionRequest req) {
<<<<<<< HEAD
        return ResponseEntity.ok(ApiResponse.ok("Request updated",
                tripService.decideRequest(id, reqId, req, caller.userId(), caller.societyId())));
    }

    @GetMapping("/{id}/participants")
    public ResponseEntity<ApiResponse<List<com.pravesh.dto.response.ParticipantResponse>>> getParticipants(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser caller) {
        return ResponseEntity.ok(ApiResponse.ok("Participants", tripService.getParticipants(id, caller.userId(), caller.societyId())));
    }

    @GetMapping("/{id}/discussion")
    public ResponseEntity<ApiResponse<List<TripCommentResponse>>> getDiscussion(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser caller) {
        return ResponseEntity.ok(ApiResponse.ok("Discussion", tripService.getDiscussion(id, caller.userId(), caller.societyId())));
    }

    @PostMapping("/{id}/discussion")
    public ResponseEntity<ApiResponse<TripCommentResponse>> addComment(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser caller,
            @Valid @RequestBody AddCommentRequest req) {
        TripCommentResponse response = tripService.addComment(id, req, caller.userId(), caller.societyId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Comment added", response));
    }
}
=======
        return ApiResponse.ok("Request updated",
                tripService.decideRequest(id, reqId, req, caller.userId(), caller.societyId()));
    }

    @GetMapping("/{id}/participants")
    public ApiResponse<List<com.pravesh.dto.response.ParticipantResponse>> getParticipants(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser caller) {
        return ApiResponse.ok("Participants", tripService.getParticipants(id, caller.userId(), caller.societyId()));
    }

    @GetMapping("/{id}/discussion")
    public ApiResponse<List<TripCommentResponse>> getDiscussion(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser caller) {
        return ApiResponse.ok("Discussion", tripService.getDiscussion(id, caller.userId(), caller.societyId()));
    }

    @PostMapping("/{id}/discussion")
    public ApiResponse<TripCommentResponse> addComment(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser caller,
            @Valid @RequestBody AddCommentRequest req) {
        return ApiResponse.ok("Comment added", tripService.addComment(id, req, caller.userId(), caller.societyId()));
    }
}
>>>>>>> origin/salonee
