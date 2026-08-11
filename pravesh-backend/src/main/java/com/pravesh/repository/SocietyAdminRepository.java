package com.pravesh.repository;

import com.pravesh.entity.SocietyAdmin;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SocietyAdminRepository extends JpaRepository<SocietyAdmin, Long> {
	
	List<SocietyAdmin> findBySocietyId(Long societyId);
}