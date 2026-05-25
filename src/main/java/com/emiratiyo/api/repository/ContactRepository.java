package com.emiratiyo.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.emiratiyo.api.entity.ContactEntity;

import java.util.List;

@Repository
public interface ContactRepository extends JpaRepository<ContactEntity, Long> {
    List<ContactEntity> findByStatus(String status);
}