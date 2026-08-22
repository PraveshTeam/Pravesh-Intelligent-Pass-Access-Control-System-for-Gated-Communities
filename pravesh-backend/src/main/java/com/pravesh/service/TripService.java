package com.pravesh.service;

import com.pravesh.dto.request.AddCommentRequest;
import com.pravesh.dto.request.ProposeTripRequest;
import com.pravesh.dto.request.RequestDecisionRequest;
import com.pravesh.dto.response.TripCommentResponse;
import com.pravesh.dto.response.JoinRequestResponse;
import com.pravesh.dto.response.ParticipantResponse;
import com.pravesh.dto.response.TripResponse;
import com.pravesh.entity.*;
import com.pravesh.exception.DuplicateResourceException;
import com.pravesh.exception.InvalidStateException;
import com.pravesh.exception.ResourceNotFoundException;
import com.pravesh.dto.response.ResidentContextResponse;
import com.pravesh.dto.response.UserContactResponse;
import com.pravesh.repository.TripCommentRepository;
import com.pravesh.repository.TripJoinRequestRepository;
import com.pravesh.repository.TripRepository;
import com.pravesh.util.EntityRefs;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TripService {

    private static final Logger log = LoggerFactory.getLogger(TripService.class);

    private final TripRepository tripRepository;
    private final TripJoinRequestRepository joinRequestRepository;
    private final TripCommentRepository commentRepository;
    private final com.pravesh.service.UserDirectoryService userDirectoryService;
    private final EntityRefs refs;


    // ---------- Browse / Propose ----------

    public List<TripResponse> listTrips(Long societyId, Long callerId) {
        List<Trip> trips = tripRepository.findBySocietyIdOrderByCreatedAtDesc(societyId);
        Map<Long, String> names = resolveNames(
                trips.stream().map(t -> t.getCreator().getId()).collect(Collectors.toSet()));
        return trips.stream().map(t -> toTripResponse(t, names, callerId)).toList();
    }

    @Transactional
    public TripResponse proposeTrip(ProposeTripRequest req, Long creatorId, Long societyId) {
        if (societyId == null) {
            throw new InvalidStateException("Could not determine your society. Please log in again.");
        }
        Trip trip = Trip.builder()
                .creator(refs.ref(User.class, creatorId))
                .society(refs.ref(Society.class, societyId))
                .title(req.title())
                .description(req.description())
                .capacity(req.capacity())
                .build();
        trip = tripRepository.save(trip);

        Map<Long, String> names = resolveNames(Set.of(creatorId));
        return toTripResponse(trip, names, creatorId);
    }

    // ---------- Join requests ----------

    @Transactional
    public JoinRequestResponse requestToJoin(Long tripId, Long requesterId, Long callerSocietyId) {
        Trip trip = tripRepository.findByIdAndSocietyId(tripId, callerSocietyId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found: " + tripId));

        if (trip.getCreator().getId().equals(requesterId)) {
            throw new InvalidStateException("You can't request to join your own trip");
        }
        if (trip.getStatus() != TripStatus.OPEN) {
            throw new InvalidStateException("This trip is no longer accepting join requests (" + trip.getStatus() + ")");
        }
        joinRequestRepository.findByTripIdAndRequester_Id(tripId, requesterId).ifPresent(existing -> {
            throw new DuplicateResourceException("You've already requested to join this trip");
        });

        TripJoinRequest jr = TripJoinRequest.builder()
                .trip(trip)
                .requester(refs.ref(User.class, requesterId))
                .build();
        jr = joinRequestRepository.save(jr);

        Map<Long, String> names = resolveNames(Set.of(requesterId));
        return toJoinRequestResponse(jr, names);
    }

    public List<JoinRequestResponse> listRequests(Long tripId, Long callerId, Long callerSocietyId) {
        Trip trip = tripRepository.findByIdAndSocietyId(tripId, callerSocietyId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found: " + tripId));
        if (!trip.getCreator().getId().equals(callerId)) {
            throw new AccessDeniedException("Only the trip creator can view its join requests");
        }
        List<TripJoinRequest> requests = joinRequestRepository.findByTripIdOrderByCreatedAtAsc(tripId);
        Map<Long, String> names = resolveNames(
                requests.stream().map(r -> r.getRequester().getId()).collect(Collectors.toSet()));
        return requests.stream().map(r -> toJoinRequestResponse(r, names)).toList();
    }

    @Transactional
    public JoinRequestResponse decideRequest(Long tripId, Long requestId, RequestDecisionRequest req,
                                              Long callerId, Long callerSocietyId) {
        Trip trip = tripRepository.findByIdAndSocietyId(tripId, callerSocietyId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found: " + tripId));
        if (!trip.getCreator().getId().equals(callerId)) {
            throw new AccessDeniedException("Only the trip creator can accept or reject requests");
        }

        TripJoinRequest jr = joinRequestRepository.findByIdAndTripId(requestId, tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Join request not found: " + requestId));

        if (jr.getStatus() != JoinRequestStatus.PENDING) {
            throw new InvalidStateException("This request has already been " + jr.getStatus());
        }

        JoinRequestStatus decision = JoinRequestStatus.valueOf(req.status());

        if (decision == JoinRequestStatus.ACCEPTED) {
            long acceptedSoFar = joinRequestRepository.countByTripIdAndStatus(tripId, JoinRequestStatus.ACCEPTED);
            if (acceptedSoFar >= trip.getCapacity()) {
                throw new InvalidStateException("Trip is already at full capacity");
            }
            jr.setStatus(JoinRequestStatus.ACCEPTED);
            joinRequestRepository.save(jr);

            // If this acceptance filled the last seat, flip the trip to FULL.
            long acceptedNow = joinRequestRepository.countByTripIdAndStatus(tripId, JoinRequestStatus.ACCEPTED);
            if (acceptedNow >= trip.getCapacity()) {
                trip.setStatus(TripStatus.FULL);
                tripRepository.save(trip);
            }
        } else {
            jr.setStatus(JoinRequestStatus.REJECTED);
            joinRequestRepository.save(jr);
        }

        Map<Long, String> names = resolveNames(Set.of(jr.getRequester().getId()));
        return toJoinRequestResponse(jr, names);
    }

    // ---------- Discussion (accepted participants + creator only) ----------

    public List<TripCommentResponse> getDiscussion(Long tripId, Long callerId, Long callerSocietyId) {
        Trip trip = tripRepository.findByIdAndSocietyId(tripId, callerSocietyId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found: " + tripId));
        assertParticipant(trip, callerId);

        List<TripComment> comments = commentRepository.findByTripIdOrderByCreatedAtAsc(tripId);
        Map<Long, String> names = resolveNames(
                comments.stream().map(c -> c.getAuthor().getId()).collect(Collectors.toSet()));
        return comments.stream().map(c -> toCommentResponse(c, names)).toList();
    }

    @Transactional
    public TripCommentResponse addComment(Long tripId, AddCommentRequest req, Long callerId, Long callerSocietyId) {
        Trip trip = tripRepository.findByIdAndSocietyId(tripId, callerSocietyId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found: " + tripId));
        assertParticipant(trip, callerId);

        TripComment comment = TripComment.builder()
                .trip(trip)
                .author(refs.ref(User.class, callerId))
                .body(req.body())
                .build();
        comment = commentRepository.save(comment);

        Map<Long, String> names = resolveNames(Set.of(callerId));
        return toCommentResponse(comment, names);
    }

    // Only the creator or an ACCEPTED requester may read/post in the discussion.
    private void assertParticipant(Trip trip, Long callerId) {
        if (trip.getCreator().getId().equals(callerId)) return;
        boolean accepted = joinRequestRepository.findByTripIdAndRequester_Id(trip.getId(), callerId)
                .map(jr -> jr.getStatus() == JoinRequestStatus.ACCEPTED)
                .orElse(false);
        if (!accepted) {
            throw new AccessDeniedException("Only accepted participants can view this trip's discussion");
        }
    }

    // ---------- Participants (name/flat/phone for everyone confirmed on the trip) ----------

    public List<ParticipantResponse> getParticipants(Long tripId, Long callerId, Long callerSocietyId) {
        Trip trip = tripRepository.findByIdAndSocietyId(tripId, callerSocietyId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found: " + tripId));
        // Same access rule as the discussion.
        assertParticipant(trip, callerId);

        // LinkedHashSet keeps the creator first, then accepted participants in join order.
        Long creatorId = trip.getCreator().getId();
        java.util.LinkedHashSet<Long> ids = new java.util.LinkedHashSet<>();
        ids.add(creatorId);
        joinRequestRepository.findByTripIdAndStatus(tripId, JoinRequestStatus.ACCEPTED)
                .forEach(jr -> ids.add(jr.getRequester().getId()));

        List<ParticipantResponse> result = new java.util.ArrayList<>();
        for (Long userId : ids) {
            try {
                ResidentContextResponse ctx = userDirectoryService.getResidentContext(userId);
                result.add(new ParticipantResponse(
                        userId,
                        ctx != null ? ctx.name() : null,
                        ctx != null ? ctx.phone() : null,
                        ctx != null ? ctx.flatNumber() : null,
                        userId.equals(creatorId)));
            } catch (Exception e) {
                // One failed lookup shouldn't hide the whole list.
                log.warn("Could not resolve participant context for {}: {}", userId, e.getMessage());
                result.add(new ParticipantResponse(userId, null, null, null, userId.equals(creatorId)));
            }
        }
        return result;
    }

    // ---------- Helpers ----------

    private Map<Long, String> resolveNames(Set<Long> userIds) {
        Map<Long, String> names = new HashMap<>();
        for (Long id : userIds) {
            try {
                UserContactResponse contact = userDirectoryService.getContact(id);
                if (contact != null) names.put(id, contact.name());
            } catch (Exception e) {
                log.warn("Could not resolve name for user {}: {}", id, e.getMessage());
            }
        }
        return names;
    }

    private TripResponse toTripResponse(Trip t, Map<Long, String> names, Long callerId) {
        long acceptedCount = joinRequestRepository.countByTripIdAndStatus(t.getId(), JoinRequestStatus.ACCEPTED);
        Long creatorId = t.getCreator().getId();
        String myRequestStatus = creatorId.equals(callerId)
                ? null
                : joinRequestRepository.findByTripIdAndRequester_Id(t.getId(), callerId)
                        .map(jr -> jr.getStatus().name())
                        .orElse(null);
        return new TripResponse(
                t.getId(), creatorId, names.get(creatorId),
                t.getTitle(), t.getDescription(), t.getCapacity(), (int) acceptedCount,
                t.getStatus(), t.getCreatedAt(), myRequestStatus);
    }

    private JoinRequestResponse toJoinRequestResponse(TripJoinRequest jr, Map<Long, String> names) {
        Long requesterId = jr.getRequester().getId();
        return new JoinRequestResponse(
                jr.getId(), jr.getTrip().getId(), requesterId, names.get(requesterId),
                jr.getStatus(), jr.getCreatedAt());
    }

    private TripCommentResponse toCommentResponse(TripComment c, Map<Long, String> names) {
        Long authorId = c.getAuthor().getId();
        return new TripCommentResponse(
                c.getId(), authorId, names.get(authorId),
                c.getBody(), c.getCreatedAt());
    }
}