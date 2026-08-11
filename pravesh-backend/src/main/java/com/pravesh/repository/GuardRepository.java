package com.pravesh.repository;

import com.pravesh.entity.Guard;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GuardRepository extends JpaRepository<Guard, Long> {
    boolean existsByGateId(Long gateId);
}