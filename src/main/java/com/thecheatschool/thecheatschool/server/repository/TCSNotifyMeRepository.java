package com.thecheatschool.thecheatschool.server.repository;

import com.thecheatschool.thecheatschool.server.model.tcs.TCSNotifyMeSignup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TCSNotifyMeRepository extends JpaRepository<TCSNotifyMeSignup, Long> {

    Optional<TCSNotifyMeSignup> findByEmail(String email);

    List<TCSNotifyMeSignup> findByStatus(String status);
}
