package com.emiratiyo.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.emiratiyo.api.entity.EmiraAnalysisEntity;

import java.util.List;

@Repository
public interface EmiraAnalysisRepository extends JpaRepository<EmiraAnalysisEntity, Long> {
    List<EmiraAnalysisEntity> findAllByOrderByCreatedAtDesc();
}