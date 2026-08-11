package com.pravesh.service;

import com.pravesh.dto.request.CreateGateRequest;
import com.pravesh.dto.response.GateResponse;
import com.pravesh.entity.Gate;
import com.pravesh.repository.GateRepository;
import com.pravesh.repository.GuardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GateService {

    private final GateRepository gateRepository;
    private final GuardRepository guardRepository;

    @Transactional
    public GateResponse createGate(CreateGateRequest req, Long societyId) {
        Gate gate = Gate.builder()
                .societyId(societyId)
                .name(req.name())
                .location(req.location())
                .build();
        gate = gateRepository.save(gate);
        return new GateResponse(gate.getId(), gate.getName(), gate.getLocation(), false);
    }

    public List<GateResponse> listGates(Long societyId, boolean unassignedOnly) {
        return gateRepository.findBySocietyId(societyId).stream()
                .map(gate -> new GateResponse(
                        gate.getId(), gate.getName(), gate.getLocation(),
                        guardRepository.existsByGateId(gate.getId())))
                .filter(g -> !unassignedOnly || !g.hasAssignedGuard())
                .toList();
    }
}