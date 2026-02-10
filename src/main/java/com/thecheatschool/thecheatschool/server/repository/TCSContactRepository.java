package com.thecheatschool.thecheatschool.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.thecheatschool.thecheatschool.server.model.tcs.TCSContact;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TCSContactRepository extends JpaRepository<TCSContact, Long> {

    // Find all failed submissions
    List<TCSContact> findByStatus(String status);

    // Find expired submissions (for cleanup)
    List<TCSContact> findByExpiresAtBefore(LocalDateTime date);
}