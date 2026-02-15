package com.thecheatschool.thecheatschool.server.repository;

import com.thecheatschool.thecheatschool.server.model.em.EMContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EMContactRepository extends JpaRepository<EMContact, Long> {

    List<EMContact> findByStatus(String status);

    List<EMContact> findByExpiresAtBefore(LocalDateTime date);
}
