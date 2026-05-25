package com.emiratiyo.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.emiratiyo.api.entity.BusinessSetupEntity;

import java.util.List;

@Repository
public interface BusinessSetupRepository extends JpaRepository<BusinessSetupEntity, Long> {
    List<BusinessSetupEntity> findByStatus(String status);
}