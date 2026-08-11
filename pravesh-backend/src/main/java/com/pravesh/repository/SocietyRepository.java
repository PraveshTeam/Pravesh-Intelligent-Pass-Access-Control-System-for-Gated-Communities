package com.pravesh.repository;

import com.pravesh.entity.Society;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SocietyRepository extends JpaRepository<Society, Long> {
	
	boolean existsByNameAndCity(String name, String city);
	List<Society> findByNameContainingIgnoreCase(String name);
}

