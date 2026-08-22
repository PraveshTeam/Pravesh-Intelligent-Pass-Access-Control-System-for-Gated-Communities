package com.pravesh.service;

import com.pravesh.dto.response.FlatResponse;
import com.pravesh.repository.FlatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FlatService {

    private final FlatRepository flatRepository;

    public List<FlatResponse> listFlats(Long societyId) {
        return flatRepository.findBySocietyId(societyId).stream()
                .map(f -> new FlatResponse(f.getId(), f.getFlatNumber(), f.getTower(),
                        f.getOccupant() != null ? f.getOccupant().getId() : null))
                .toList();
    }
}