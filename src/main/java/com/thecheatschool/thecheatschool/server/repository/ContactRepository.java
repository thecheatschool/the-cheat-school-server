package com.thecheatschool.thecheatschool.server.repository;

import com.thecheatschool.thecheatschool.server.model.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {

    // Find all failed submissions
    List<Contact> findByStatus(String status);

    // Find expired submissions (for cleanup)
    List<Contact> findByExpiresAtBefore(LocalDateTime date);
}